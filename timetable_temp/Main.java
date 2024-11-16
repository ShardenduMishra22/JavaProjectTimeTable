import java.io.Serializable;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.*;
import java.nio.file.*;
import java.awt.Desktop;
import java.util.Random;

@SuppressWarnings("unused")
class Class implements Serializable {
    private String batchName;
    private String classroom;
    private Course course;
    private TimeSlot timeSlot;
    private boolean isLab;

    public Class(String batchName, String classroom, Course course, TimeSlot timeSlot, boolean isLab) {
        this.batchName = batchName;
        this.classroom = classroom;
        this.course = course;
        this.timeSlot = timeSlot;
        this.isLab = isLab;
    }

    // Getters1
    public String getBatchName() { return batchName; }
    public String getClassroom() { return classroom; }
    public Course getCourse() { return course; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public boolean isLab() { return isLab; }
}

class Course implements Serializable {
    private String id;
    private String courseCode;
    private String name;
    private String courseType;
    private String branch;
    private String section;
    private int lecture;
    private int theory;
    private int practical;
    private String credits;
    private int hoursPerWeek;
    private List<String> eligibleFacultyIds;

    public Course(String id, String courseCode, String name, String courseType, String branch, String section,
            int lecture, int theory, int practical, String credits, int hoursPerWeek, List<String> eligibleFacultyIds) {
        this.id = id;
        this.courseCode = courseCode;
        this.name = name;
        this.courseType = courseType;
        this.branch = branch;
        this.section = section;
        this.lecture = lecture;
        this.theory = theory;
        this.practical = practical;
        this.credits = credits;
        this.hoursPerWeek = hoursPerWeek;
        this.eligibleFacultyIds = eligibleFacultyIds;
    }

    // Getters
    public String getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public String getName() { return name; }
    public String getCourseType() { return courseType; }
    public String getBranch() { return branch; }
    public String getSection() { return section; }
    public int getLecture() { return lecture; }
    public int getTheory() { return theory; }
    public int getPractical() { return practical; }
    public String getCredits() { return credits; }
    public int getHoursPerWeek() { return hoursPerWeek; }
    public List<String> getEligibleFacultyIds() { return eligibleFacultyIds; }
}

class TimeSlot implements Serializable {
    private String day;
    private String startTime;
    private String endTime;

    public TimeSlot(String day, String startTime, String endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    @Override
    public String toString() {
        return day + " " + startTime + "-" + endTime;
    }
}

// Old TimeTable implementation
class OldTimeTable implements Serializable {
    private Map<String, List<Class>> classes;
    private Map<String, List<TimeSlot>> teacherSchedule;
    private static final String[] DAYS = {"MON", "TUE", "WED", "THU", "FRI"};
    private static final String[] TIME_SLOTS = {"09:00-10:30", "10:45-12:15", "14:30-16:00", "16:00-17:30"};

    public OldTimeTable() {
        classes = new HashMap<>();
        teacherSchedule = new HashMap<>();
    }
    
    public boolean isEmpty() {
        return classes.isEmpty();
    }

    public boolean isSlotFree(TimeSlot newSlot, String batchName, String facultyId) {
        if (classes.containsKey(batchName)) {
            for (Class cls : classes.get(batchName)) {
                if (cls.getTimeSlot().getDay().equals(newSlot.getDay()) &&
                    isTimeOverlap(cls.getTimeSlot(), newSlot)) {
                    return false;
                }
            }
        }
        
        if (teacherSchedule.containsKey(facultyId)) {
            for (TimeSlot existingSlot : teacherSchedule.get(facultyId)) {
                if (existingSlot.getDay().equals(newSlot.getDay()) &&
                    isTimeOverlap(existingSlot, newSlot)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private boolean isTimeOverlap(TimeSlot slot1, TimeSlot slot2) {
        int start1 = timeToMinutes(slot1.getStartTime());
        int end1 = timeToMinutes(slot1.getEndTime());
        int start2 = timeToMinutes(slot2.getStartTime());
        int end2 = timeToMinutes(slot2.getEndTime());

        return (start1 < end2 && start2 < end1);
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    @SuppressWarnings("unused")
    public boolean addClass(Class newClass, String branch) {
        String batchName = newClass.getBatchName();
        if (isSlotFree(newClass.getTimeSlot(), batchName, newClass.getCourse().getEligibleFacultyIds().get(0))) {
            classes.computeIfAbsent(batchName, k -> new ArrayList<>()).add(newClass);
            
            for (String facultyId : newClass.getCourse().getEligibleFacultyIds()) {
                teacherSchedule.computeIfAbsent(facultyId, k -> new ArrayList<>())
                              .add(newClass.getTimeSlot());
            }
            return true;
        }
        return false;
    }

    public void displayTimetable(String batchName) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                       Timetable for " + batchName + "                                                       ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║   Time   │   Monday   │   Tuesday  │  Wednesday │  Thursday  │   Friday   ║");
        System.out.println("╠══════════╪════════════╪════════════╪════════════╪════════════╪════════════╣");

        for (String timeSlot : TIME_SLOTS) {
            System.out.printf("║ %8s │", timeSlot);
            for (String day : DAYS) {
                boolean slotFilled = false;
                for (Class cls : classes.getOrDefault(batchName, Collections.emptyList())) {
                    if (cls.getTimeSlot().getDay().equals(day) &&
                        isTimeOverlap(cls.getTimeSlot(), new TimeSlot(day, timeSlot.split("-")[0], timeSlot.split("-")[1]))) {
                        System.out.printf(" %-10s │", cls.getCourse().getCourseCode());
                        slotFilled = true;
                        break;
                    }
                }
                if (!slotFilled) {
                    System.out.print("           │");
                }
            }
            System.out.println();
        }

        System.out.println("╚══════════╧════════════╧════════════╧════════════╧════════════╧════════════╝");
        System.out.println("\nCourse Details:");
        System.out.println("═══════════════");

        for (Class cls : classes.getOrDefault(batchName, Collections.emptyList())) {
            Course course = cls.getCourse();
            System.out.printf("%-10s: %s (%s)\n", course.getCourseCode(), course.getName(), course.getCredits());
            System.out.printf("           Type: %s, Branch: %s, Section: %s\n", course.getCourseType(), course.getBranch(), course.getSection());
            System.out.printf("           Hours: L-%d T-%d P-%d\n", course.getLecture(), course.getTheory(), course.getPractical());
            System.out.printf("           Eligible Faculty IDs: %s\n", String.join(", ", course.getEligibleFacultyIds()));
            System.out.println();
        }
    }

    public List<TimeSlot> findFreeSlots(String batchName, String day) {
        List<TimeSlot> freeSlots = new ArrayList<>();
        List<TimeSlot> occupiedSlots = new ArrayList<>();

        for (Class cls : classes.getOrDefault(batchName, Collections.emptyList())) {
            if (cls.getTimeSlot().getDay().equals(day)) {
                occupiedSlots.add(cls.getTimeSlot());
            }
        }

        occupiedSlots.sort(Comparator.comparing(TimeSlot::getStartTime));

        String[] standardTimes = {"09:00", "10:45", "12:15", "14:30", "16:00", "17:30"};
        
        for (int i = 0; i < standardTimes.length - 1; i++) {
            TimeSlot potentialSlot = new TimeSlot(day, standardTimes[i], standardTimes[i+1]);
            boolean isFree = true;

            for (TimeSlot occupiedSlot : occupiedSlots) {
                if (isTimeOverlap(potentialSlot, occupiedSlot)) {
                    isFree = false;
                    break;
                }
            }

            if (isFree) {
                freeSlots.add(potentialSlot);
            }
        }

        return freeSlots;
    }

    public void scheduleLunch() {
        Random random = new Random();
        for (String batchName : classes.keySet()) {
            String day = DAYS[random.nextInt(DAYS.length)];
            int lunchHour = 12 + random.nextInt(2);
            int lunchMinute = 30 + random.nextInt(2) * 30;
            String lunchStart = "12:30";
            String lunchEnd = String.format("%02d:%02d", lunchHour, lunchMinute);
            TimeSlot lunchSlot = new TimeSlot(day, lunchStart, lunchEnd);
            Course lunchCourse = new Course("LUNCH", "LUNCH", "Lunch Break", "BREAK", "", "", 0, 0, 0, "", 0, new ArrayList<>());
            Class lunchClass = new Class(batchName, "CANTEEN", lunchCourse, lunchSlot, false);
            classes.get(batchName).add(lunchClass);
        }
    }
}

// New TimeTable implementation supporting multiple branches
class NewTimeTable implements Serializable {
    private Map<String, Map<String, List<Class>>> branchClasses;
    private Map<String, List<TimeSlot>> teacherSchedule;
    private static final String[] DAYS = {"MON", "TUE", "WED", "THU", "FRI"};
    private static final String[] TIME_SLOTS = {"09:00-10:30", "10:45-12:15", "14:30-16:00", "16:00-17:30"};

    public NewTimeTable() {
        branchClasses = new HashMap<>();
        teacherSchedule = new HashMap<>();
    }
    
    public boolean isEmpty() {
        return branchClasses.isEmpty();
    }

    public boolean isSlotFree(TimeSlot newSlot, String branch, String batchName, String facultyId) {
        if (branchClasses.containsKey(branch) && branchClasses.get(branch).containsKey(batchName)) {
            for (Class cls : branchClasses.get(branch).get(batchName)) {
                if (cls.getTimeSlot().getDay().equals(newSlot.getDay()) &&
                    isTimeOverlap(cls.getTimeSlot(), newSlot)) {
                    return false;
                }
            }
        }
        
        if (teacherSchedule.containsKey(facultyId)) {
            for (TimeSlot existingSlot : teacherSchedule.get(facultyId)) {
                if (existingSlot.getDay().equals(newSlot.getDay()) &&
                    isTimeOverlap(existingSlot, newSlot)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private boolean isTimeOverlap(TimeSlot slot1, TimeSlot slot2) {
        int start1 = timeToMinutes(slot1.getStartTime());
        int end1 = timeToMinutes(slot1.getEndTime());
        int start2 = timeToMinutes(slot2.getStartTime());
        int end2 = timeToMinutes(slot2.getEndTime());

        return (start1 < end2 && start2 < end1);
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    @SuppressWarnings("unused")
    public boolean addClass(Class newClass, String branch) {
        String batchName = newClass.getBatchName();
        if (isSlotFree(newClass.getTimeSlot(), branch, batchName, newClass.getCourse().getEligibleFacultyIds().get(0))) {
            branchClasses
                .computeIfAbsent(branch, k -> new HashMap<>())
                .computeIfAbsent(batchName, k -> new ArrayList<>())
                .add(newClass);
            
            for (String facultyId : newClass.getCourse().getEligibleFacultyIds()) {
                teacherSchedule.computeIfAbsent(facultyId, k -> new ArrayList<>())
                              .add(newClass.getTimeSlot());
            }
            return true;
        }
        return false;
    }

    public void displayTimetable(String branch, String batchName) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                 Timetable for " + branch + " - " + batchName + "                                            ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║   Time   │   Monday   │   Tuesday  │  Wednesday │  Thursday  │   Friday   ║");
        System.out.println("╠══════════╪════════════╪════════════╪════════════╪════════════╪════════════╣");

        for (String timeSlot : TIME_SLOTS) {
            System.out.printf("║ %8s │", timeSlot);
            for (String day : DAYS) {
                boolean slotFilled = false;
                if (branchClasses.containsKey(branch) && branchClasses.get(branch).containsKey(batchName)) {
                    for (Class cls : branchClasses.get(branch).get(batchName)) {
                        if (cls.getTimeSlot().getDay().equals(day) &&
                            isTimeOverlap(cls.getTimeSlot(), new TimeSlot(day, timeSlot.split("-")[0], timeSlot.split("-")[1]))) {
                            System.out.printf(" %-10s │", cls.getCourse().getCourseCode());
                            slotFilled = true;
                            break;
                        }
                    }
                }
                if (!slotFilled) {
                    System.out.print("           │");
                }
            }
            System.out.println();
        }

        System.out.println("╚══════════╧════════════╧════════════╧════════════╧════════════╧════════════╝");
        System.out.println("\nCourse Details:");
        System.out.println("═══════════════");

        if (branchClasses.containsKey(branch) && branchClasses.get(branch).containsKey(batchName)) {
            for (Class cls : branchClasses.get(branch).get(batchName)) {
                Course course = cls.getCourse();
                System.out.printf("%-10s: %s (%s)\n", course.getCourseCode(), course.getName(), course.getCredits());
                System.out.printf("           Type: %s, Branch: %s, Section: %s\n", course.getCourseType(), course.getBranch(), course.getSection());
                System.out.printf("           Hours: L-%d T-%d P-%d\n", course.getLecture(), course.getTheory(), course.getPractical());
                System.out.printf("           Eligible Faculty IDs: %s\n", String.join(", ", course.getEligibleFacultyIds()));
                System.out.println();
            }
        }
    }

    public List<TimeSlot> findFreeSlots(String branch, String batchName, String day) {
        List<TimeSlot> freeSlots = new ArrayList<>();
        List<TimeSlot> occupiedSlots = new ArrayList<>();

        if (branchClasses.containsKey(branch) && branchClasses.get(branch).containsKey(batchName)) {
            for (Class cls : branchClasses.get(branch).get(batchName)) {
                if (cls.getTimeSlot().getDay().equals(day)) {
                    occupiedSlots.add(cls.getTimeSlot());
                }
            }
        }

        occupiedSlots.sort(Comparator.comparing(TimeSlot::getStartTime));

        String[] standardTimes = {"09:00", "10:45", "12:15", "14:30", "16:00", "17:30"};
        
        for (int i = 0; i < standardTimes.length - 1; i++) {
            TimeSlot potentialSlot = new TimeSlot(day, standardTimes[i], standardTimes[i+1]);
            boolean isFree = true;

            for (TimeSlot occupiedSlot : occupiedSlots) {
                if (isTimeOverlap(potentialSlot, occupiedSlot)) {
                    isFree = false;
                    break;
                }
            }

            if (isFree) {
                freeSlots.add(potentialSlot);
            }
        }

        return freeSlots;
    }

    public void scheduleLunch() {
        Random random = new Random();
        for (String branch : branchClasses.keySet()) {
            for (String batchName : branchClasses.get(branch).keySet()) {
                String day = DAYS[random.nextInt(DAYS.length)];
                int lunchHour = 12 + random.nextInt(2);
                int lunchMinute = 30 + random.nextInt(2) * 30;
                String lunchStart = "12:30";
                String lunchEnd = String.format("%02d:%02d", lunchHour, lunchMinute);
                TimeSlot lunchSlot = new TimeSlot(day, lunchStart, lunchEnd);
                Course lunchCourse = new Course("LUNCH", "LUNCH", "Lunch Break", "BREAK", "", "", 0, 0, 0, "", 0, new ArrayList<>());
                Class lunchClass = new Class(batchName, "CANTEEN", lunchCourse, lunchSlot, false);
                branchClasses.get(branch).get(batchName).add(lunchClass);
            }
        }
    }
}

public class Main {
    private static final String DATA_FOLDER = "../timetable_data";
    private static final String DATA_FILE = DATA_FOLDER + File.separator + "timetable.dat";
    private static final String COURSES_CSV = DATA_FOLDER + File.separator + "courses.csv";
    private static final String BATCHES_CSV = DATA_FOLDER + File.separator + "batches.csv";
    private static final int MAX_RETRIES = 10;

    public static void main(String[] args) {
        NewTimeTable timeTable = loadTimeTable();
        if (timeTable.isEmpty()) {
            timeTable = createTimeTableFromCSV();
        }
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Timetable Management System");
            System.out.println("1. View Timetable");
            System.out.println("2. View Free Slots");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    viewTimetable(scanner, timeTable);
                    break;
                case 2:
                    findFreeSlots(scanner, timeTable);
                    break;
                case 3:
                    saveTimeTable(timeTable);
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void viewTimetable(Scanner scanner, NewTimeTable timeTable) {
        System.out.print("Enter branch (e.g., CSE, ECE, DSAI): ");
        String branch = scanner.nextLine().toUpperCase();
        System.out.print("Enter batch name to view: ");
        String batchName = scanner.nextLine();
        timeTable.displayTimetable(branch, batchName);
    }

    private static void findFreeSlots(Scanner scanner, NewTimeTable timeTable) {
        System.out.print("Enter branch (e.g., CSE, ECE, DSAI): ");
        String branch = scanner.nextLine().toUpperCase();
        System.out.print("Enter batch name: ");
        String batchName = scanner.nextLine();
        System.out.print("Enter day (MON/TUE/WED/THU/FRI): ");
        String day = scanner.nextLine().toUpperCase();
        List<TimeSlot> freeSlots = timeTable.findFreeSlots(branch, batchName, day);
        System.out.println("Free slots for " + branch + " - " + batchName + " on " + day + ":");
        for (TimeSlot slot : freeSlots) {
            System.out.println(slot);
        }
    }

    private static NewTimeTable createTimeTableFromCSV() {
        NewTimeTable timeTable = new NewTimeTable();
        Map<String, Course> coursesMap = new HashMap<>();
        Map<String, Batch> batchesMap = new HashMap<>();

        try {
            // Read courses
            List<String> courseLines = Files.readAllLines(Paths.get(COURSES_CSV));
            System.out.println("Number of course lines: " + courseLines.size());
            if (!courseLines.isEmpty()) {
                courseLines.remove(0); // Remove header
            }
            for (String line : courseLines) {
                String[] parts = line.split(",");
                if (parts.length >= 12) {
                    try {
                        Course course = new Course(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5],
                                                   Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
                                                   Integer.parseInt(parts[8]), parts[9], Integer.parseInt(parts[10]),
                                                   Arrays.asList(parts[11].split(";")));
                        coursesMap.put(course.getId(), course);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing course line: " + line);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Invalid course line (not enough fields): " + line);
                }
            }
            System.out.println("Number of courses loaded: " + coursesMap.size());

            // Read batches
            List<String> batchLines = Files.readAllLines(Paths.get(BATCHES_CSV));
            System.out.println("Number of batch lines: " + batchLines.size());
            if (!batchLines.isEmpty()) {
                batchLines.remove(0); // Remove header
            }
            for (String line : batchLines) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    try {
                        Batch batch = new Batch(parts[0], parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                                                Arrays.asList(parts[4].split(";")),
                                                Arrays.asList(parts[5].split(";")),
                                                Arrays.asList(parts[6].split(";")));
                        batchesMap.put(batch.getId(), batch);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing batch line: " + line);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Invalid batch line (not enough fields): " + line);
                }
            }
            System.out.println("Number of batches loaded: " + batchesMap.size());

            // Generate timetable
            String[] days = {"MON", "TUE", "WED", "THU", "FRI"};
            String[] timeSlots = {"09:00-10:30", "10:45-12:15", "14:30-16:00", "16:00-17:30"};

            for (Batch batch : batchesMap.values()) {
                for (String courseId : batch.getCourseIds()) {
                    Course course = coursesMap.get(courseId);
                    if (course != null) {
                        int totalHours = course.getLecture() + course.getTheory() + course.getPractical();
                        for (int i = 0; i < totalHours; i++) {
                            boolean added = false;
                            int retries = 0;
                            while (!added && retries < MAX_RETRIES) {
                                String day = days[new Random().nextInt(days.length)];
                                String timeSlot = timeSlots[new Random().nextInt(timeSlots.length)];
                                String[] times = timeSlot.split("-");
                                String classroom = course.getCourseType().equalsIgnoreCase("practical") ?
                                                   batch.getPracticalRoomIDs().get(new Random().nextInt(batch.getPracticalRoomIDs().size())) :
                                                   batch.getLectureRoomIDs().get(new Random().nextInt(batch.getLectureRoomIDs().size()));

                                Class newClass = new Class(
                                    batch.getBatchName(),
                                    classroom,
                                    course,
                                    new TimeSlot(day, times[0], times[1]),
                                    course.getCourseType().equalsIgnoreCase("practical")
                                );

                                if (timeTable.addClass(newClass, course.getBranch())) {
                                    System.out.println("Added " + course.getCourseCode() + " to " + day + " " + timeSlot + " for " + course.getBranch() + " - " + batch.getBatchName());
                                    added = true;
                                } else {
                                    retries++;
                                }
                            }
                            if (!added) {
                                System.out.println("Unable to add " + course.getCourseCode() + " for " + course.getBranch() + " - " + batch.getBatchName() + " after " + MAX_RETRIES + " attempts.");
                            }
                        }
                    }
                }
            }
            timeTable.scheduleLunch();
        } catch (IOException e) {
            System.out.println("Error reading CSV files: " + e.getMessage());
            e.printStackTrace();
        }
        return timeTable;
    }

    private static NewTimeTable loadTimeTable() {
        File dataFile = new File(DATA_FILE);
        if (dataFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
                return (NewTimeTable) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading timetable data. Starting with a new timetable.");
            }
        }
        return new NewTimeTable();
    }

    private static void saveTimeTable(NewTimeTable timeTable) {
        File dataFolder = new File(DATA_FOLDER);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(timeTable);
            System.out.println("Timetable data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving timetable data: " + e.getMessage());
        }
    }

    private static class Batch {
        private String id;
        private String batchName;
        private int year;
        private int strength;
        private List<String> courseIds;
        private List<String> lectureRoomIDs;
        private List<String> practicalRoomIDs;

        public Batch(String id, String batchName, int year, int strength, 
                     List<String> courseIds, List<String> lectureRoomIDs, List<String> practicalRoomIDs) {
            this.id = id;
            this.batchName = batchName;
            this.year = year;
            this.strength = strength;
            this.courseIds = courseIds;
            this.lectureRoomIDs = lectureRoomIDs;
            this.practicalRoomIDs = practicalRoomIDs;
        }
        // Getters
        public String getId() { return id; }
        public String getBatchName() { return batchName; }
        @SuppressWarnings("unused")
        public int getYear() { return year; }
        @SuppressWarnings("unused")
        public int getStrength() { return strength; }
        public List<String> getCourseIds() { return courseIds; }
        public List<String> getLectureRoomIDs() { return lectureRoomIDs; }
        public List<String> getPracticalRoomIDs() { return practicalRoomIDs; }
    }
}