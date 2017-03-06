package de.eiswind.xino.ui.navigation.event


import de.eiswind.xino.ui.navigation.DashboardViewType

/*
 * Event bus events used in Dashboard are listed here as inner classes.
 */
abstract class DashboardEvent {

    //    public static final class UserLoginRequestedEvent {
    //        private final String userName, password;
    //
    //        public UserLoginRequestedEvent(final String userName,
    //                final String password) {
    //            this.userName = userName;
    //            this.password = password;
    //        }
    //
    //        public String getUserName() {
    //            return userName;
    //        }
    //
    //        public String getPassword() {
    //            return password;
    //        }
    //    }
    //
    //    public static class BrowserResizeEvent {
    //
    //    }
    //
    //    public static class UserLoggedOutEvent {
    //
    //    }
    //
    //    public static class NotificationsCountUpdatedEvent {
    //    }
    //
    //    public static final class ReportsCountUpdatedEvent {
    //        private final int count;
    //
    //        public ReportsCountUpdatedEvent(final int count) {
    //            this.count = count;
    //        }
    //
    //        public int getCount() {
    //            return count;
    //        }
    //
    //    }


    class PostViewChangeEvent(val view: DashboardViewType)

    //    public static class CloseOpenWindowsEvent {
    //    }
    //
    //    public static class ProfileUpdatedEvent {
    //    }

}
