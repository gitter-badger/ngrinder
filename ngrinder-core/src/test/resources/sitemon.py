from net.grinder.script.Grinder import grinder
from net.grinder.script import Test

test1 = Test(1, "test script")
grinder.statistics.registerSummaryExpression("User_defined1", "(/ userLong1(+ (count timedTests)))")

class TestRunner:
	def __init__(self):
		grinder.statistics.delayReports=True
		test1.record(TestRunner.request)
	def __call__(self):
		self.request()
		grinder.statistics.forLastTest.setLong("userLong1", 1)
		grinder.statistics.forLastTest.success = 1		
	def request(self):
		pass