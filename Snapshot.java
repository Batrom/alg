import java.util.Map;
import java.util.Set;

record Snapshot(
        // timeslot to (companyId to meetingRoom)
        Map<Long, Map<Long, MeetingRoom>> soloMeetings,
        // timeslot to (companyId to meetingRoom)
        Map<Long, Map<Long, MeetingRoom>> groupMeetings,
        // userId to timeslots
        Map<Long, Set<Long>> usersAvailableTimeslots,
        // companyId to timeslots
        Map<Long, Set<Long>> companiesAvailableTimeslots,
        // timeslot to (capacity to count)
        Map<Long, Map<Integer, Integer>> timeslotsFreeRooms) {
}
