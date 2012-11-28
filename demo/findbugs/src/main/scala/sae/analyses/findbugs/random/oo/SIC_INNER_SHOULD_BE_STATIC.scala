package sae.analyses.findbugs.random.oo

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 14:47
 *
 */
//TODO was this in the list of random analyses?
object SIC_INNER_SHOULD_BE_STATIC
{
    /*
        // RM: fills data about classes that cannot be static
        if (getSuperclassName().indexOf("$") >= 0 || getSuperclassName().indexOf("+") >= 0
                || withinAnonymousClass.matcher(getDottedClassName()).find()) {
            data.innerClassCannotBeStatic.add(getDottedClassName());
            data.innerClassCannotBeStatic.add(getDottedSuperclassName());
        }

        // RM: finds out whether the super constructor uses the reference to outer.this
        if (getMethodName().equals("<init>") && count_aload_1 > 1
                && (getClassName().indexOf('$') >= 0 || getClassName().indexOf('+') >= 0)) {
            data.needsOuterObjectInConstructor.add(getDottedClassName());
            // System.out.println(betterClassName +
            // " needs outer object in constructor");
        }

        // RM: actual bug reporting
        if (!data.innerClassCannotBeStatic.contains(className)) {
            boolean easyChange = !data.needsOuterObjectInConstructor.contains(className);
            if (easyChange || !isAnonymousInnerClass) {

                // easyChange isAnonymousInnerClass
                // true false medium, SIC
                // true true low, SIC_ANON
                // false true not reported
                // false false low, SIC_THIS
                int priority = LOW_PRIORITY;
                if (easyChange && !isAnonymousInnerClass)
                    priority = NORMAL_PRIORITY;

                String bug = "SIC_INNER_SHOULD_BE_STATIC";
                if (isAnonymousInnerClass)
                    bug = "SIC_INNER_SHOULD_BE_STATIC_ANON";
                else if (!easyChange)
                    bug = "SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS";

                bugReporter.reportBug(new BugInstance(this, bug, priority).addClass(className));

            }
        }
     */
}