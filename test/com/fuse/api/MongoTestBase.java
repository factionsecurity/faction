package com.fuse.api;

import java.util.UUID;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Assume;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.Note;
import com.fuse.dao.Permissions;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Teams;

/**
 * Base test class that provides a MongoDB connection for tests.
 * Uses Testcontainers with MongoDB if Docker is available, otherwise
 * connects to a user-provided MongoDB instance via environment variables.
 * 
 * Test database name: "faction-test"
 * 
 * Usage with Testcontainers (Docker required):
 *   mvn test
 * 
 * Usage with external MongoDB:
 *   export FACTION_MONGO_HOST=localhost
 *   export FACTION_MONGO_PORT=27017
 *   export FACTION_MONGO_DATABASE=faction-test
 *   mvn test
 * 
 * To start an external MongoDB instance:
 *   docker run -d --name mongo-test -p 27017:27017 mongo:5.0
 *   export FACTION_MONGO_HOST=localhost
 *   export FACTION_MONGO_PORT=27017
 *   export FACTION_MONGO_DATABASE=faction-test
 *   mvn test
 */
public abstract class MongoTestBase {

    protected static MongoDBContainer mongoDBContainer;
    protected static boolean usingExternalMongo = false;

    @BeforeClass
    public static void startMongoDB() {
        String host = System.getProperty("FACTION_MONGO_HOST");
        String port = System.getProperty("FACTION_MONGO_PORT");
        String database = System.getProperty("FACTION_MONGO_DATABASE");
        
        // Check if external MongoDB is configured
        if (host != null && !host.isEmpty() && port != null && !port.isEmpty() && database != null && !database.isEmpty()) {
            System.out.println("Using external MongoDB: " + host + ":" + port + "/" + database);
            System.setProperty("FACTION_MONGO_HOST", host);
            System.setProperty("FACTION_MONGO_PORT", port);
            System.setProperty("FACTION_MONGO_DATABASE", database);
            System.setProperty("FACTION_MONGO_USER", "");
            System.setProperty("FACTION_MONGO_PASSWORD", "");
            System.setProperty("FACTION_MONGO_AUTH_DATABASE", "");
            System.setProperty("FACTION_MONGO_SSL", "false");
            usingExternalMongo = true;
            return;
        }
        
        // Try to use Testcontainers
        try {
            System.out.println("Attempting to start MongoDB via Testcontainers...");
            mongoDBContainer = new MongoDBContainer(
                DockerImageName.parse("mongo:8")
            );
            mongoDBContainer.withReuse(true);
            mongoDBContainer.start();

            System.setProperty("FACTION_MONGO_HOST", mongoDBContainer.getHost());
            System.setProperty("FACTION_MONGO_PORT", String.valueOf(mongoDBContainer.getMappedPort(27017)));
            System.setProperty("FACTION_MONGO_DATABASE", "faction-test");
            System.setProperty("FACTION_MONGO_USER", "");
            System.setProperty("FACTION_MONGO_PASSWORD", "");
            System.setProperty("FACTION_MONGO_AUTH_DATABASE", "");
            System.setProperty("FACTION_MONGO_SSL", "false");
            System.out.println("MongoDB started via Testcontainers: " + mongoDBContainer.getHost() + ":" + mongoDBContainer.getMappedPort(27017));
        } catch (IllegalStateException e) {
            Assume.assumeNoException(
                "Skipping MongoDB-dependent tests: Docker is not available. " +
                "To run these tests, start Docker and run: mvn test\n" +
                "Or use an external MongoDB:\n  export FACTION_MONGO_HOST=localhost\n" +
                "  export FACTION_MONGO_PORT=27017\n  export FACTION_MONGO_DATABASE=faction-test\n  mvn test",
                e
            );
        }
    }

    @AfterClass
    public static void stopMongoDB() {
        if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
            System.out.println("Stopping MongoDB container...");
            mongoDBContainer.stop();
        }
    }

    // --- Factory methods for test entities ---

    public static User createTestUser(long id, String username, String fname, String lname, String email,
                                      boolean isAssessor, boolean isManager, boolean isEngagement, boolean isAdmin) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFname(fname);
        user.setLname(lname);
        user.setEmail(email);
        Permissions perms = new Permissions();
        perms.setAssessor(isAssessor);
        perms.setManager(isManager);
        perms.setEngagement(isEngagement);
        perms.setAdmin(isAdmin);
        user.setPermissions(perms);
        return user;
    }

    public static AssessmentType createTestAssessmentType(long id, String type) {
        AssessmentType at = new AssessmentType();
        at.setId(id);
        at.setType(type);
        return at;
    }

    public static Campaign createTestCampaign(long id, String name) {
        Campaign camp = new Campaign();
        camp.setId(id);
        camp.setName(name);
        return camp;
    }

    public static Category createTestCategory(long id, String name) {
        Category cat = new Category();
        cat.setId(id);
        cat.setName(name);
        return cat;
    }

    public static CustomType createTestCustomType(String key, int fieldType, int objType) {
        CustomType ct = new CustomType();
        ct.setKey(key);
        ct.setFieldType(fieldType);
        ct.setType(objType);
        return ct;
    }

    public static CustomField createTestCustomField(CustomType type, String value) {
        CustomField cf = new CustomField();
        cf.setType(type);
        cf.setValue(value);
        return cf;
    }

    public static Note createTestNote(String noteText) {
        Note note = new Note();
        note.setNote(noteText);
        note.setCreated(new Date());
        return note;
    }

    public static Vulnerability createTestVulnerability(long id, String name, long overall, long impact, long likelyhood,
                                                         String tracking, Category category) {
        Vulnerability v = new Vulnerability();
        v.setId(id);
        v.setName(name);
        v.setOverall(overall);
        v.setImpact(impact);
        v.setLikelyhood(likelyhood);
        v.setTracking(tracking);
        v.setCategory(category);
        v.setAssessmentId(1L);
        v.setCreated(new Date());
        return v;
    }

    public static RiskLevel createTestRiskLevel(int riskId, String risk) {
        RiskLevel rl = new RiskLevel();
        rl.setRiskId(riskId);
        rl.setRisk(risk);
        return rl;
    }

    public static Assessment createTestAssessment(long id, String name, String appId, AssessmentType type,
                                                   List<User> assessors, User engagement, User remediation,
                                                   Campaign campaign, List<Vulnerability> vulns) {
        Assessment a = new Assessment();
        a.setId(id);
        a.setName(name);
        a.setAppId(appId);
        a.setType(type);
        a.setAssessor(assessors);
        a.setEngagement(engagement);
        a.setRemediation(remediation);
        a.setCampaign(campaign);
        a.setVulns(vulns);
        a.setStart(new Date());
        a.setEnd(new Date());
        a.setStatus("In Progress");

        List<Note> notebook = new ArrayList<>();
        notebook.add(createTestNote("Test notes"));
        a.setNotebook(notebook);

        return a;
    }

    public static Teams createTestTeam(long id, String name) {
        Teams team = new Teams();
        team.setId(id);
        team.setTeamName(name);
        return team;
    }
}
