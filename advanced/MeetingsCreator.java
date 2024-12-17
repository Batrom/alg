package advanced;

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
        var meetings = extractMeetings();

        for (var pair : context.pairs()) {
            meetings = pickBestMeetings(pair, meetings);
        }

        return flattenMeetings(meetings);
    }

    private static List<Meeting> flattenMeetings(final List<Map<Pair, Meeting>> meetings) {
        return meetings.isEmpty() ? List.of() : meetings.getFirst().values().stream().distinct().toList();
    }

    private static List<Map<Pair, Meeting>> pickBestMeetings(final Pair pair, final List<Map<Pair, Meeting>> meetings) {
        final var soloMeetings = meetings.stream()
                .filter(entry -> isSoloMeeting(pair, entry))
                .toList();

        if (!soloMeetings.isEmpty()) {
            return soloMeetings;
        }

        return meetings;
    }

    private static boolean isSoloMeeting(final Pair pair, final Map<Pair, Meeting> entry) {
        final var meeting = entry.get(pair);
        return meeting != null && meeting.solo();
    }

    private List<Map<Pair, Meeting>> extractMeetings() {
        return snapshots.snapshots()
                .stream()
                .map(Meeting::from)
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
