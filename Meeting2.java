import java.util.HashSet;
import java.util.Set;

record Meeting2(Set<Integer> userIds, int companyId, int timeslot, boolean allowGroups) {

    static Meeting2 group(final Integer userId, final int companyId, final int timeslot) {
        return new Meeting2(setOf(userId), companyId, timeslot, true);
    }

    static Meeting2 solo(final Integer userId, final int companyId, final int timeslot) {
        return new Meeting2(setOf(userId), companyId, timeslot, false);
    }

    private static Set<Integer> setOf(final Integer userId) {
        final var userIds = new HashSet<Integer>();
        userIds.add(userId);
        return userIds;
    }

    void addUser(final int userId) {
        userIds.add(userId);
    }

    void removeUser(final int userId) {
        userIds.remove(userId);
    }
}
