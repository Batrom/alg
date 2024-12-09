import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

class BestMeetingsPicker {

    static List<Meeting> findBestMatchedMeetings(final List<Pair> pairs, final List<Snapshot> snapshots) {
        var meetings = extractMeetings(snapshots);

        for (var pair : pairs) {
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

    private static List<Map<Pair, Meeting>> extractMeetings(final List<Snapshot> snapshots) {
        return snapshots
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