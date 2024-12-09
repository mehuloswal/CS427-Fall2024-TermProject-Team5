package edu.uiuc.cs427app;

import android.os.IBinder;
import android.view.WindowManager;
import androidx.test.espresso.Root;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom matcher for verifying Toast messages in Espresso tests.
 *
 * This matcher checks if a `Root` is a Toast by verifying the window type and ensuring
 * that the window token matches the application window token.
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {

    /**
     * Describes the matcher for error messages when a match fails.
     *
     * @param description The description of the matcher.
     */
    @Override
    public void describeTo(Description description) {
        description.appendText("is a Toast");
    }

    /**
     * Matches a given `Root` to determine if it is a Toast.
     *
     * @param root The `Root` to be matched.
     * @return True if the `Root` is a Toast, otherwise false.
     */
    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            return windowToken == appToken;
        }
        return false;
    }
}
