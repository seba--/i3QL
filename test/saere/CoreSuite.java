package saere;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import saere.predicate.TestAnd2;
import saere.predicate.TestCompoundTermStateManifestation;

@RunWith(Suite.class)
@SuiteClasses({ TestAnd2.class, TestCompoundTermStateManifestation.class,TestVariableSharingWithManifestation.class })
public class CoreSuite {
	// nothing to do
}
