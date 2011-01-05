package saere;

public abstract class PrimitiveGoal implements Goal {

	@Override
	public final boolean choiceCommitted() {
		return false;
	}

}
