package saere;

public abstract class GoalStack {

	private final static class EmptyGoalStack extends GoalStack {

		public EmptyGoalStack() {
			// nothing to do
		}

		@Override
		public GoalStack put(Solutions solutions) {
			return new SomeGoalStack(this, solutions);
		}

		@Override
		public Solutions peek() {
			throw new IllegalStateException("the goal stack is empty");
		}

		@Override
		public GoalStack reduce() {
			throw new IllegalStateException("the goal stack is empty");
		}
	}

	private final static class SomeGoalStack extends GoalStack {

		private final GoalStack goalStack;
		private final Solutions solutions;

		public SomeGoalStack(GoalStack goalStack, Solutions solutions) {
			this.goalStack = goalStack;
			this.solutions = solutions;
		}

		@Override
		public GoalStack put(Solutions furtherSolutions) {
			return new SomeGoalStack(this, furtherSolutions);
		}

		@Override
		public Solutions peek() {
			return solutions;
		}

		@Override
		public GoalStack reduce() {
			return goalStack;
		}

	}

	private final static EmptyGoalStack EMPTY_GOAL_STACK = new EmptyGoalStack();

	public abstract GoalStack put(Solutions solutions);

	public abstract Solutions peek();

	public abstract GoalStack reduce();

	public static GoalStack emptyStack() {
		return EMPTY_GOAL_STACK;
	}
}
