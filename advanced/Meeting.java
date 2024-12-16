package advanced;

import java.util.Set;

public record Meeting(long timeslot, long companyId, long roomId, Set<Long> userIds, boolean solo) {

    static Meeting from(final MeetingWithoutRoom meetingWithoutRoom, final long roomId) {
        return new Meeting(meetingWithoutRoom.timeslot(),
                meetingWithoutRoom.companyId(),
                roomId,
                meetingWithoutRoom.userIds(),
                meetingWithoutRoom.solo());
    }
}