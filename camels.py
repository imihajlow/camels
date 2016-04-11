#!/usr/bin/python3

nCamels = 5
nPlayers = 1
nSteps = 10

class State:
	_dice = []
	_camels = []
	_plusOnes = []
	_minusOnes = []

	def __init__(self, dice, camels, plusOnes, minusOnes):
		self._dice = dice
		self._camels = camels
		self._plusOnes = plusOnes
		self._minusOnes = minusOnes

	def jump(self, camel, steps):
		newDice = self._dice[:]
		newDice[camel] = False
		xFrom, yFrom = self._camels[camel]
		xTo = xFrom + steps
		under = False
		if xTo in self._plusOnes:
			xTo += 1
		elif xTo in self._minusOnes:
			xTo -= 1
			under = True
		newCamels = [None for _ in range(nCamels)]
		if under:
			yOffset = sum(1 for x,y in self._camels if x == xFrom and y >= yFrom)
			for i in range(nCamels):
				x,y = self._camels[i]
				if x == xFrom and y >= yFrom:
					newCamels[i] = xTo, y - yFrom
				elif x == xTo:
					newCamels[i] = x, y + yOffset
				else:
					newCamels[i] = x, y
		else:
			yOffset = sum(1 for x,y in self._camels if x == xTo)
			for i in range(nCamels):
				x,y = self._camels[i]
				if x == xFrom and y >= yFrom:
					newCamels[i] = xTo, yOffset + y - yFrom
				else:
					newCamels[i] = x, y
		return State(newDice, newCamels, self._plusOnes, self._minusOnes)

	def plusOne(self, x):
		return State(self._dice, self._camels, self._plusOnes | {x}, self._minusOnes)

	def minusOne(self, x):
		return State(self._dice, self._camels, self._plusOnes, self._minusOnes | {x})

def positions(state, result):
	if not any(state._dice):
		sortedCamels = sorted(enumerate(state._camels), key=lambda c: c[1][0] * nCamels + c[1][1], reverse=True)
		sortedCamels = [a for a,b in sortedCamels]
		position = 0
		for c in sortedCamels:
			result[c][position] += 1
			position += 1
		return 1
	else:
		counter = 0
		for i in (i for i,x in enumerate(state._dice) if x):
			for value in range(1,7):
				counter += positions(state.jump(i, value), result)
		"""
		minCamel = min(x for x,y in state._camels)
		xs = set(x for x,y in state._camels)
		if len(state._plusOnes) + len(state._minusOnes) < nPlayers:
			for x in range(minCamel + 1, nSteps):
				if x not in xs and not any(xa in state._plusOnes or xa in state._minusOnes for xa in [x-1, x, x+1]):
					counter += positions(state.plusOne(x), result)
					counter += positions(state.minusOne(x), result)
		"""
		return counter

def test():
	s = State([True for _ in range(nCamels)], [(0,0),(0,1),(1,1),(1,0),(2,0)], [], [])
	s1 = s.jump(3, 1)
	assert(s1._camels == [(0,0), (0,1), (2,2), (2,1), (2,0)])

	s = State([True for _ in range(nCamels)], [(0,0),(0,1),(1,1),(1,0),(2,0)], [], [3])
	s1 = s.jump(3, 2)
	print(s1._camels)
	assert(s1._camels == [(0,0), (0,1), (2,1), (2,0), (2,2)])

if __name__ == "__main__":
	test()
	result = [[0 for place in range(nCamels)] for camel in range(nCamels)]
	n = positions(State([True for _ in range(nCamels)], [(0,0),(0,1),(1,0),(1,1),(2,0)], set([]), set([3])), result)
	result = [[a/n for a in x] for x in result]
	for i,l in enumerate(result):
		print("camel {0}: [{1}]".format(i+1, ", ".join("{0:.3f}".format(x) for x in l)))