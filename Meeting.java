import java.util.Arrays;
import java.util.Objects;

record Meeting(int[] userIds, int companyId, int timeslot, boolean allowGroups) {

    Meeting addUser(final int userId) {
        return new Meeting(MatchingHelper.add(userIds, userId), companyId, timeslot, allowGroups);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Meeting meeting = (Meeting) o;
        return timeslot == meeting.timeslot &&
               companyId == meeting.companyId &&
               allowGroups == meeting.allowGroups &&
               Objects.deepEquals(userIds, meeting.userIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(userIds), companyId, timeslot, allowGroups);
    }
}
