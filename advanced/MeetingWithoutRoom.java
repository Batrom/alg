package advanced;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record MeetingWithoutRoom(long timeslot, long companyId, int roomCapacity, Set<Long> userIds, boolean solo) {

    static Set<Meeting> toMeetings(final Snapshot snapshot) {
        return Stream.concat(toGroupMeetings(snapshot), toSoloMeetings(snapshot)).collect(Collectors.toSet());
    }

    private static Stream<Meeting> toGroupMeetings(final Snapshot snapshot) {
        return toMeetings(snapshot.groupMeetings(), false);
    }

    private static Stream<Meeting> toSoloMeetings(final Snapshot snapshot) {
        return toMeetings(snapshot.soloMeetings(), true);
    }

    private static Stream<Meeting> toMeetings(final Map<Long, Map<Long, MeetingRoom>> meetingsMap, final boolean solo) {
        return meetingsMap
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .entrySet()
                        .stream()
                        .map(innerEntry ->
                                new Meeting(entry.getKey(),
                                        innerEntry.getKey(),
                                        innerEntry.getValue().room().id(),
                                        new HashSet<>(innerEntry.getValue().userIds()),
                                        solo))
                );
    }
}