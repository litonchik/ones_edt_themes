package ru.litonchik.edt.themes.ui;

import org.eclipse.core.expressions.PropertyTester;
import ru.litonchik.edt.themes.runtime.UpdateNotifications;

public final class UpdateAvailablePropertyTester extends PropertyTester {
    @Override public boolean test(Object receiver,String property,Object[] args,Object expectedValue){return "updateAvailable".equals(property) && UpdateNotifications.available();}
}
