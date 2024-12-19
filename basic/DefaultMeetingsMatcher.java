package basic;

final class DefaultMeetingsMatcher extends MeetingsMatcher {

    DefaultMeetingsMatcher(final MeetingsMatcherContext context) {
        super(context);
    }

    @Override
    protected Boolean findAlternatives(final long userId, final Meeting meeting) {
        final var alternativeMeetingsMatcher = AlternativeMeetingsMatcher.basedOn(this);
        final var alternativesFound = alternativeMeetingsMatcher.findAlternatives(userId, meeting);
        if (alternativesFound) {
            this.context = alternativeMeetingsMatcher.context;
            return true;
        } else return null;
    }

    @Override
    protected boolean pairIsLocked(final long timeslot, final long companyId) {
        return false;
    }
}
