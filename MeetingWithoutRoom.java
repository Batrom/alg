import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record MeetingWithoutRoom(long timeslot, long companyId, int roomCapacity, Set<Long> userIds, boolean solo) {

    static Set<MeetingWithoutRoom> toMeetings(final Snapshot snapshot) {
        return Stream.concat(toGroupMeetings(snapshot), toSoloMeetings(snapshot)).collect(Collectors.toSet());
    }

    private static Stream<MeetingWithoutRoom> toGroupMeetings(final Snapshot snapshot) {
        return toMeetings(snapshot.groupMeetings(), false);
    }

    private static Stream<MeetingWithoutRoom> toSoloMeetings(final Snapshot snapshot) {
        return toMeetings(snapshot.soloMeetings(), true);
    }

    private static Stream<MeetingWithoutRoom> toMeetings(final Map<Long, Map<Long, MeetingRoom>> meetingsMap, final boolean solo) {
        return meetingsMap
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .entrySet()
                        .stream()
                        .map(innerEntry ->
                                new MeetingWithoutRoom(entry.getKey(),
                                        innerEntry.getKey(),
                                        innerEntry.getValue().capacity(),
                                        new HashSet<>(innerEntry.getValue().userIds()),
                                        solo))
                );
    }
}