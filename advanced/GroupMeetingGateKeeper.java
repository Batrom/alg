package advanced;

import java.util.Set;

record GroupMeetingGateKeeper(Set<Long> usersThatAllowGroupMeetings,
                              Set<Long> companiesThatAllowGroupMeetings) {

    boolean bothAllowsGroupMeetings(final long userId, final long companyId) {
        return usersThatAllowGroupMeetings.contains(userId) && companiesThatAllowGroupMeetings.contains(companyId);
    }
}
