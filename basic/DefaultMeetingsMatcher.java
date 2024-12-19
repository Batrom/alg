package basic;

final class DefaultMeetingsMatcher extends MeetingsMatcher {

    DefaultMeetingsMatcher(final MeetingsMatcherContext context) {
        super(context);
    }

    @Override
    protected Boolean findAlternatives(final long userId, final Meeting meeting) {
        return AlternativeMeetingsMatcher.basedOn(this).findAlternatives(userId, meeting) ? true : null;
    }

    @Override
    protected boolean pairIsLocked(final long timeslot, final long companyId) {
        return false;
    }
}
