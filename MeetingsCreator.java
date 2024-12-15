import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

class MeetingsCreator {
    private final Context context;
    private final Snapshots snapshots;

    MeetingsCreator(final Context context, final Snapshots snapshots) {
        this.context = context;
        this.snapshots = snapshots;
    }

    List<Meeting> createMeetings() {
        final var meetings = pickMeetings();
        final var timeslotsRooms = createTimeslotsRooms();

        return meetings.stream()
                .map(meeting -> createMeeting(meeting, timeslotsRooms))
                .toList();
    }

    private static Meeting createMeeting(final MeetingWithoutRoom meeting, final Map<Long, Map<Integer, Long>> timeslotsRooms) {
        return Meeting.from(meeting, timeslotsRooms.get(meeting.timeslot()).get(meeting.roomCapacity()));
    }

    private Map<Long, Map<Integer, Long>> createTimeslotsRooms() {
        return context.timeslots().stream().collect(toMap(Timeslot::id, e -> e.rooms().stream().collect(toMap(Room::capacity, Room::id))));
    }

    private List<MeetingWithoutRoom> pickMeetings() {
        var meetings = extractMeetings();

        for (var pair : context.pairs()) {
            meetings = pickBestMeetings(pair, meetings);
        }

        return flattenMeetings(meetings);
    }

    private static List<MeetingWithoutRoom> flattenMeetings(final List<Map<Pair, MeetingWithoutRoom>> meetings) {
        return meetings.isEmpty() ? List.of() : meetings.getFirst().values().stream().toList();
    }

    private static List<Map<Pair, MeetingWithoutRoom>> pickBestMeetings(final Pair pair, final List<Map<Pair, MeetingWithoutRoom>> meetings) {
        final var soloMeetings = meetings.stream()
                .filter(entry -> isSoloMeeting(pair, entry))
                .toList();

        if (!soloMeetings.isEmpty()) {
            return soloMeetings;
        }

        return meetings;
    }

    private static boolean isSoloMeeting(final Pair pair, final Map<Pair, MeetingWithoutRoom> entry) {
        final var meeting = entry.get(pair);
        return meeting != null && meeting.solo();
    }

    private List<Map<Pair, MeetingWithoutRoom>> extractMeetings() {
        return snapshots.snapshots()
                .stream()
                .map(MeetingWithoutRoom::toMeetings)
                .map(meetings ->
                        meetings.stream()
                                .flatMap(meeting ->
                                        meeting.userIds()
                                                .stream()
                                                .map(userId -> Map.entry(new Pair(userId, meeting.companyId()), meeting)))
                                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .toList();
    }
}
