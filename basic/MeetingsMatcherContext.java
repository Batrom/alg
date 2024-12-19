package basic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

record MeetingsMatcherContext(
        RoomsHolder roomsHolder,
        TimeslotsHolder timeslotsHolder,
        Map<Long, Map<Long, Meeting>> soloMeetings,
        Map<Long, Map<Long, Meeting>> groupMeetings,
        Map<Long, Set<Long>> usersAvailableTimeslots,
        Map<Long, Set<Long>> companiesAvailableTimeslots,
        Set<Long> usersThatAllowGroupMeetings,
        Set<Long> companiesThatAllowGroupMeetings,
        Comparator<Long> usersComparator
) {

    MeetingsMatcherContext deepCopy() {
        return new MeetingsMatcherContext(
                copyRoomsHolder(),
                timeslotsHolder,
                copyMeetings(soloMeetings),
                copyMeetings(groupMeetings),
                copyTimeslots(usersAvailableTimeslots),
                copyTimeslots(companiesAvailableTimeslots),
                usersThatAllowGroupMeetings,
                companiesThatAllowGroupMeetings,
                usersComparator);
    }

    private RoomsHolder copyRoomsHolder() {
        return new RoomsHolder(roomsHolder.timeslotRooms()
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, entry -> new LinkedList<>(entry.getValue()))));
    }

    private Map<Long, Map<Long, Meeting>> copyMeetings(final Map<Long, Map<Long, Meeting>> meetings) {
        return meetings.entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .entrySet()
                                .stream()
                                .collect(toMap(
                                        Map.Entry::getKey,
                                        nestedEntry -> copyMeeting(nestedEntry.getValue())))
                ));
    }

    private Map<Long, Set<Long>> copyTimeslots(final Map<Long, Set<Long>> timeslots) {
        return timeslots.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, entry -> new HashSet<>(entry.getValue())));
    }

    private static Meeting copyMeeting(final Meeting meeting) {
        return new Meeting(
                meeting.timeslot(),
                meeting.companyId(),
                new ArrayList<>(meeting.userIds()),
                meeting.room(),
                meeting.solo()
        );
    }

    private static <T, K, U> Collector<T, ?, Map<K, U>> toMap(final Function<? super T, ? extends K> keyMapper,
                                                              final Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, takeFirst(), HashMap::new);
    }

    private static <T> BinaryOperator<T> takeFirst() {
        return (first, second) -> first;
    }
}
