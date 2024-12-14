import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

class BestMeetingsPicker {
    private final MatcherContext context;
    private final SnapshotsTracker snapshotsTracker;

    BestMeetingsPicker(final MatcherContext context, final SnapshotsTracker snapshotsTracker) {
        this.context = context;
        this.snapshotsTracker = snapshotsTracker;
    }

    List<Meeting> pickMeetings() {
        var meetings = extractMeetings();

        for (var pair : context.pairs()) {
            meetings = extractSoloMeetingsIfExist(pair, meetings);
        }

        return flattenMeetings(meetings);
    }

    private static List<Meeting> flattenMeetings(final List<Map<Pair, Meeting>> meetings) {
        return meetings.isEmpty() ? List.of() : meetings.getFirst().values().stream().toList();
    }

    private static List<Map<Pair, Meeting>> extractSoloMeetingsIfExist(final Pair pair, final List<Map<Pair, Meeting>> meetings) {
        final var soloMeetings = meetings.stream()
                .filter(entry -> isSoloMeeting(pair, entry))
                .toList();

        if (!soloMeetings.isEmpty()) {
            return soloMeetings;
        }

        return meetings;
    }

    private static boolean isSoloMeeting(Pair pair, Map<Pair, Meeting> entry) {
        final var meeting = entry.get(pair);
        return meeting != null && meeting.userIds().length == 1;
    }

    private List<Map<Pair, Meeting>> extractMeetings() {
        return snapshotsTracker.snapshots()
                .stream()
                .map(Snapshot::uniqueMeetings)
                .map(meetings ->
                        meetings.stream()
                                .flatMap(meeting ->
                                        Arrays.stream(meeting.userIds())
                                                .mapToObj(userId -> Map.entry(new Pair(userId, meeting.companyId()), meeting)))
                                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .toList();
    }
}
