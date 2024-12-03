

public static List<Meeting> scheduleMeetings(List<User> users, List<Company> companies) {
    // Sort users by rating (high to low)
    users.sort((u1, u2) -> Integer.compare(u2.rating, u1.rating));

    List<Meeting> meetings = new ArrayList<>();
    Map<Integer, Set<Integer>> occupiedUserSlots = new HashMap<>(); // userId -> timeSlots
    Map<Integer, Set<Integer>> occupiedCompanySlots = new HashMap<>(); // companyId -> timeSlots

    for (User user : users) {
        for (int i = 0; i < user.companyPreferences.size(); i++) {
            int companyId = user.companyPreferences.get(i);
            Company company = companies.stream().filter(c -> c.id == companyId).findFirst().orElse(null);
            if (company == null) continue;

            List<Integer> availableSlots = new ArrayList<>();
            for (int timeSlot : user.availableTimeSlots) {
                if (company.availableTimeSlots.contains(timeSlot) &&
                        !occupiedUserSlots.getOrDefault(user.id, new HashSet<>()).contains(timeSlot) &&
                        !occupiedCompanySlots.getOrDefault(companyId, new HashSet<>()).contains(timeSlot)) {
                    availableSlots.add(timeSlot);
                }
            }

            // Find the best slot for this user considering others
            Integer chosenSlot = null;
            for (int timeSlot : availableSlots) {
                boolean slotNeededByLowerRated = false;

                // Check if a lower-rated user needs this slot
                for (User otherUser : users) {
                    if (otherUser.rating < user.rating && otherUser.companyPreferences.contains(companyId)) {
                        if (otherUser.availableTimeSlots.contains(timeSlot) &&
                                !occupiedUserSlots.getOrDefault(otherUser.id, new HashSet<>()).contains(timeSlot)) {
                            slotNeededByLowerRated = true;
                            break;
                        }
                    }
                }

                if (!slotNeededByLowerRated) {
                    chosenSlot = timeSlot; // Choose this slot as it's not critical for others
                    break;
                }
            }

            if (chosenSlot == null && !availableSlots.isEmpty()) {
                // No slots free of conflict, choose the first available slot
                chosenSlot = availableSlots.get(0);
            }

            if (chosenSlot != null) {
                if (!user.acceptsGroupMeetings || !company.acceptsGroupMeetings) {
                    // Schedule a one-on-one meeting
                    meetings.add(new Meeting(chosenSlot, Collections.singletonList(user.id), companyId, false));
                    occupiedUserSlots.computeIfAbsent(user.id, k -> new HashSet<>()).add(chosenSlot);
                    occupiedCompanySlots.computeIfAbsent(companyId, k -> new HashSet<>()).add(chosenSlot);
                } else {
                    // Try to form a group meeting
                    Meeting existingMeeting = meetings.stream()
                            .filter(m -> m.timeSlot == chosenSlot && m.companyId == companyId && m.isGroupMeeting)
                            .findFirst().orElse(null);

                    if (existingMeeting != null) {
                        existingMeeting.userIds.add(user.id);
                        occupiedUserSlots.computeIfAbsent(user.id, k -> new HashSet<>()).add(chosenSlot);
                    } else {
                        meetings.add(new Meeting(chosenSlot, new ArrayList<>(List.of(user.id)), companyId, true));
                        occupiedUserSlots.computeIfAbsent(user.id, k -> new HashSet<>()).add(chosenSlot);
                        occupiedCompanySlots.computeIfAbsent(companyId, k -> new HashSet<>()).add(chosenSlot);
                    }
                }
            }
        }
    }

    return meetings;
}




I have a list of users and list of companies. Users want to meet with companies. Each user defines a list of companies that he wants to meet with. The position of a company on the list defines preferences of a meeting for the user - higher on the list are companies more important for a user. User also defines list of time slots when he is available. User can meet with a company only in a time slot that is defined that he is available. Additionally user defines if he can join group meetings (true or false flag). If he doesn't accept group meetings then he can only be part of one on one meetings with company - otherwise he can be part of meetings where there're multiple users and single company. Additionally each user has a property rating.
Each company has a list of time slots that it is available. Additionally company defines if it can join group meetings (true or false flag). If it doesn't accept group meetings then it can only be part of one on one meetings with user - otherwise it can be part of meetings where there're multiple users and single company. 

User can be part of only one meeting in a given time slot. The same rule applies to company - it can be part of only one meeting in a given slot.

A meeting has properties: time slot, list of users, company, flag if it is a group meeting.

Create a list of meetings that matches companies and users in a most effective way. Try to match as many companies and users as you can.

Here are additional rules that has to be taken into account when creating meetings (order of these rules matters - higher on the list are more important rules):
- meetings for users with higher rating are more valuable than meetings for users with lower rating 
- meetings with higher user preference (position on the list of companies defined by each user) are more valuable than meetings with lower user preference
- non group meetings are more valuable than group meetings

Write an algorithm in java.

-------------------------------

adjust the algorithm so it is more flexible - lets consider a case when there is higher rated user and lower rated user that both wants to meet with the same company. But higher rated user has more available time slots so can pick other a time slot that will let lower rated user to also meet with this company (lower rated user didn't have other options). In other words - make higher rated users take into consideration of lower rated users time slots so everyone can meet with a company (if that's possible). But besides that the rules mentioned before still applies.
