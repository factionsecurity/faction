package com.fuse.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fuse.api.dto.CustomFieldDTO;
import com.fuse.api.dto.DefaultVulnerabilityDTO;
import com.fuse.dao.APIKeys;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Permissions;
import com.fuse.dao.User;

/**
 * Integration tests for vulnerabilities REST endpoints that touch the persistence layer.
 *
 * Brings up MongoDB via Testcontainers (or an external instance via FACTION_MONGO_*
 * sysprops/env), persists a test user + API key, then exercises the new
 * DELETE /default/{id}, POST /default/{id} (single-update), POST /default (list upload),
 * and POST /csv/default endpoints end-to-end through the resource class.
 */
public class VulnerabilitiesIntegrationTest extends MongoTestBase {

    private static EntityManagerFactory emf;
    private static String apiKey;
    private static Long testUserId;
    private static Long testCategoryId;
    private static List<Long> createdVulnIds = new ArrayList<>();
    private static List<Long> createdCustomTypeIds = new ArrayList<>();

    @BeforeClass
    public static void setupTestFixtures() {
        emf = HibHelper.getInstance().getEMF();
        org.junit.Assume.assumeNotNull(
                "EntityManagerFactory unavailable — skipping integration test",
                emf);

        EntityManager em = emf.createEntityManager();
        try {
            User user = new User();
            user.setUsername("vuln-it-" + UUID.randomUUID().toString().substring(0, 8));
            user.setEmail("vuln-it@example.test");
            Permissions perms = new Permissions();
            perms.setAdmin(true);
            perms.setManager(true);
            perms.setAssessor(true);
            user.setPermissions(perms);
            HibHelper.getInstance().preJoin();
            em.joinTransaction();
            em.persist(user);
            HibHelper.getInstance().commit();
            testUserId = user.getId();

            apiKey = "vuln-it-key-" + UUID.randomUUID();
            APIKeys keys = new APIKeys();
            keys.setKey(apiKey);
            keys.setUser(user);
            keys.setCreated(new Date());
            HibHelper.getInstance().preJoin();
            em.joinTransaction();
            em.persist(keys);
            HibHelper.getInstance().commit();

            Category cat = new Category();
            cat.setName("IT-Test-Category-" + UUID.randomUUID().toString().substring(0, 8));
            HibHelper.getInstance().preJoin();
            em.joinTransaction();
            em.persist(cat);
            HibHelper.getInstance().commit();
            testCategoryId = cat.getId();

            // Custom type for vulnerability templates so custom-field merging has something to match.
            CustomType ct = new CustomType();
            ct.setKey("CWE");
            ct.setVariable("cwe");
            ct.setType(CustomType.ObjType.VULN.getValue());
            ct.setDeleted(false);
            HibHelper.getInstance().preJoin();
            em.joinTransaction();
            em.persist(ct);
            HibHelper.getInstance().commit();
            createdCustomTypeIds.add(ct.getId());
        } finally {
            em.close();
        }
    }

    @AfterClass
    public static void cleanupTestFixtures() {
        if (emf == null) return;
        EntityManager em = emf.createEntityManager();
        try {
            // Best-effort cleanup of anything we created.
            for (Long id : createdVulnIds) {
                DefaultVulnerability dv = em.find(DefaultVulnerability.class, id);
                if (dv != null) {
                    HibHelper.getInstance().preJoin();
                    em.joinTransaction();
                    em.remove(dv);
                    HibHelper.getInstance().commit();
                }
            }
            for (Long id : createdCustomTypeIds) {
                CustomType ct = em.find(CustomType.class, id);
                if (ct != null) {
                    HibHelper.getInstance().preJoin();
                    em.joinTransaction();
                    em.remove(ct);
                    HibHelper.getInstance().commit();
                }
            }
            if (testCategoryId != null) {
                Category c = em.find(Category.class, testCategoryId);
                if (c != null) {
                    HibHelper.getInstance().preJoin();
                    em.joinTransaction();
                    em.remove(c);
                    HibHelper.getInstance().commit();
                }
            }
            if (testUserId != null) {
                User u = em.find(User.class, testUserId);
                if (u != null) {
                    APIKeys k = (APIKeys) em
                            .createQuery("from APIKeys where userid = :uid")
                            .setParameter("uid", testUserId)
                            .getResultList().stream().findFirst().orElse(null);
                    if (k != null) {
                        HibHelper.getInstance().preJoin();
                        em.joinTransaction();
                        em.remove(k);
                        HibHelper.getInstance().commit();
                    }
                    HibHelper.getInstance().preJoin();
                    em.joinTransaction();
                    em.remove(u);
                    HibHelper.getInstance().commit();
                }
            }
        } finally {
            em.close();
        }
    }

    private Long persistDefaultVuln(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            DefaultVulnerability dv = new DefaultVulnerability();
            dv.setName(name);
            dv.setDescription("integration test description");
            dv.setRecommendation("integration test recommendation");
            dv.setOverall(3);
            dv.setImpact(3);
            dv.setLikelyhood(3);
            dv.setActive(true);
            Category cat = em.find(Category.class, testCategoryId);
            dv.setCategory(cat);
            HibHelper.getInstance().preJoin();
            em.joinTransaction();
            em.persist(dv);
            HibHelper.getInstance().commit();
            createdVulnIds.add(dv.getId());
            return dv.getId();
        } finally {
            em.close();
        }
    }

    // --- DELETE /api/vulnerabilities/default/{id} ---

    @Test
    public void deleteDefaultVulnRemovesRecord() {
        Long vulnId = persistDefaultVuln("IT-Delete-" + UUID.randomUUID().toString().substring(0, 8));

        Response response = new vulnerabilities().deleteDefaultVuln(apiKey, vulnId);
        assertEquals("expected 200 on successful delete", 200, response.getStatus());

        EntityManager em = emf.createEntityManager();
        try {
            DefaultVulnerability deleted = em.find(DefaultVulnerability.class, vulnId);
            assertNull("vulnerability must be removed from the database", deleted);
        } finally {
            em.close();
        }
    }

    @Test
    public void deleteDefaultVulnReturns404ForUnknownId() {
        Response response = new vulnerabilities().deleteDefaultVuln(apiKey, 999_999_999L);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void deleteDefaultVulnReturns401WithoutValidApiKey() {
        Long vulnId = persistDefaultVuln("IT-Auth-" + UUID.randomUUID().toString().substring(0, 8));
        Response response = new vulnerabilities().deleteDefaultVuln("not-a-real-key", vulnId);
        assertEquals(401, response.getStatus());
    }

    // --- POST /api/vulnerabilities/default/{id} (single update) ---

    @Test
    public void updateDefaultVulnPersistsAllFields() {
        Long vulnId = persistDefaultVuln("IT-Update-" + UUID.randomUUID().toString().substring(0, 8));

        DefaultVulnerabilityDTO dto = new DefaultVulnerabilityDTO();
        dto.setName("IT-Updated-Name");
        dto.setCategoryId(testCategoryId);
        dto.setDescription("updated description");
        dto.setRecommendation("updated recommendation");
        dto.setSeverityId(5);
        dto.setImpactId(4);
        dto.setLikelihoodId(2);
        dto.setActive(false);
        dto.setCvss31Score("9.8");
        dto.setCvss31String("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H");
        dto.setCvss40Score("9.5");
        dto.setCvss40String("CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N");

        Response response = new vulnerabilities().updateDefaultVuln(apiKey, vulnId, dto);
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity() instanceof DefaultVulnerabilityDTO);
        DefaultVulnerabilityDTO returned = (DefaultVulnerabilityDTO) response.getEntity();
        assertEquals("IT-Updated-Name", returned.getName());
        assertEquals(Integer.valueOf(5), returned.getSeverityId());
        assertEquals(Integer.valueOf(4), returned.getImpactId());
        assertEquals(Integer.valueOf(2), returned.getLikelihoodId());
        assertEquals(Boolean.FALSE, returned.getActive());
        assertEquals("9.8", returned.getCvss31Score());

        EntityManager em = emf.createEntityManager();
        try {
            DefaultVulnerability stored = em.find(DefaultVulnerability.class, vulnId);
            assertNotNull(stored);
            assertEquals("IT-Updated-Name", stored.getName());
            assertEquals(5, stored.getOverall());
            assertEquals(4, stored.getImpact());
            assertEquals(2, stored.getLikelyhood());
            assertEquals(Boolean.FALSE, stored.getActive());
            assertEquals("9.8", stored.getCvss31Score());
            assertEquals("9.5", stored.getCvss40Score());
            assertTrue("description should contain submitted text",
                    stored.getDescription() != null
                            && stored.getDescription().contains("updated description"));
        } finally {
            em.close();
        }
    }

    @Test
    public void updateDefaultVulnReturns404ForUnknownId() {
        DefaultVulnerabilityDTO dto = new DefaultVulnerabilityDTO();
        dto.setName("ghost");
        dto.setCategoryId(testCategoryId);
        Response response = new vulnerabilities().updateDefaultVuln(apiKey, 888_888_888L, dto);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void updateDefaultVulnReturns400WhenNameMissing() {
        Long vulnId = persistDefaultVuln("IT-Validate-" + UUID.randomUUID().toString().substring(0, 8));
        DefaultVulnerabilityDTO dto = new DefaultVulnerabilityDTO();
        dto.setName("");
        dto.setCategoryId(testCategoryId);
        Response response = new vulnerabilities().updateDefaultVuln(apiKey, vulnId, dto);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void updateDefaultVulnPersistsCustomFields() {
        Long vulnId = persistDefaultVuln("IT-CF-" + UUID.randomUUID().toString().substring(0, 8));

        DefaultVulnerabilityDTO dto = new DefaultVulnerabilityDTO();
        dto.setName("IT-CF-Updated");
        dto.setCategoryId(testCategoryId);
        dto.setDescription("d");
        dto.setRecommendation("r");

        CustomFieldDTO cwe = new CustomFieldDTO();
        cwe.setKey("CWE");
        cwe.setValue("89");
        List<CustomFieldDTO> fields = new ArrayList<>();
        fields.add(cwe);
        dto.setCustomFields(fields);

        Response response = new vulnerabilities().updateDefaultVuln(apiKey, vulnId, dto);
        assertEquals(200, response.getStatus());

        EntityManager em = emf.createEntityManager();
        try {
            DefaultVulnerability stored = em.find(DefaultVulnerability.class, vulnId);
            assertNotNull(stored);
            assertNotNull(stored.getCustomFields());
            CustomField cweStored = stored.getCustomFields().stream()
                    .filter(cf -> cf.getType() != null && "CWE".equals(cf.getType().getKey()))
                    .findFirst().orElse(null);
            assertNotNull("CWE custom field should be persisted", cweStored);
            assertEquals("89", cweStored.getValue());
        } finally {
            em.close();
        }
    }

    // --- POST /api/vulnerabilities/default (list upload) ---

    @Test
    public void uploadJSONListCreatesNewVulnerabilities() {
        DefaultVulnerabilityDTO dto = new DefaultVulnerabilityDTO();
        dto.setName("IT-Create-" + UUID.randomUUID().toString().substring(0, 8));
        dto.setCategoryId(testCategoryId);
        dto.setDescription("create via JSON upload");
        dto.setRecommendation("rec");
        dto.setSeverityId(2);
        dto.setImpactId(2);
        dto.setLikelihoodId(2);
        dto.setActive(true);

        List<DefaultVulnerabilityDTO> list = new ArrayList<>();
        list.add(dto);
        Response response = new vulnerabilities().uploadDefaultJSONVulns(apiKey, list);
        assertEquals(200, response.getStatus());

        EntityManager em = emf.createEntityManager();
        try {
            DefaultVulnerability stored = (DefaultVulnerability) em
                    .createQuery("from DefaultVulnerability where name = :name")
                    .setParameter("name", dto.getName())
                    .getResultList().stream().findFirst().orElse(null);
            assertNotNull("uploaded vuln should be persisted", stored);
            createdVulnIds.add(stored.getId());
            assertEquals(2, stored.getOverall());
        } finally {
            em.close();
        }
    }

    @Test
    public void uploadJSONListAcceptsNullSeverityWithoutNPE() {
        // Regression: prior code unboxed null Integer into primitive int and threw NPE.
        DefaultVulnerabilityDTO dto = new DefaultVulnerabilityDTO();
        dto.setName("IT-NullSev-" + UUID.randomUUID().toString().substring(0, 8));
        dto.setCategoryId(testCategoryId);
        dto.setDescription("d");
        dto.setRecommendation("r");
        // SeverityId / ImpactId / LikelihoodId / Active intentionally left null.

        List<DefaultVulnerabilityDTO> list = new ArrayList<>();
        list.add(dto);
        Response response = new vulnerabilities().uploadDefaultJSONVulns(apiKey, list);
        assertEquals("upload should succeed even with null severity-class fields",
                200, response.getStatus());

        EntityManager em = emf.createEntityManager();
        try {
            DefaultVulnerability stored = (DefaultVulnerability) em
                    .createQuery("from DefaultVulnerability where name = :name")
                    .setParameter("name", dto.getName())
                    .getResultList().stream().findFirst().orElse(null);
            assertNotNull(stored);
            createdVulnIds.add(stored.getId());
        } finally {
            em.close();
        }
    }

    // --- POST /api/vulnerabilities/csv/default ---

    @Test
    public void uploadCSVWithHeaderAndCustomFieldsRoundTrips() {
        String name = "IT-CSV-" + UUID.randomUUID().toString().substring(0, 8);
        String csv = "Id,Name,CategoryId,CategoryName,Description,Recommendation,"
                + "SeverityId,ImpactId,LikelihoodId,isActive,CVSS31Score,CVSS31String,"
                + "CVSS40Score,CVSS40String,CustomFields\n"
                + "\"\",\"" + name + "\",\"" + testCategoryId + "\",\"\","
                + "\"<p>desc</p>\",\"<p>rec</p>\","
                + "\"4\",\"3\",\"2\",\"true\",\"7.5\",\"CVSS:3.1/AV:N\","
                + "\"7.0\",\"CVSS:4.0/AV:N\","
                + "\"[{\"\"Key\"\":\"\"CWE\"\",\"\"Value\"\":\"\"22\"\"}]\"\n";

        Response response = new vulnerabilities().uploadDefaultCSVVulns(apiKey, csv);
        assertEquals(200, response.getStatus());

        EntityManager em = emf.createEntityManager();
        try {
            DefaultVulnerability stored = (DefaultVulnerability) em
                    .createQuery("from DefaultVulnerability where name = :name")
                    .setParameter("name", name)
                    .getResultList().stream().findFirst().orElse(null);
            assertNotNull("CSV upload must create the row", stored);
            createdVulnIds.add(stored.getId());

            assertEquals(4, stored.getOverall());
            assertEquals(3, stored.getImpact());
            assertEquals(2, stored.getLikelyhood());
            assertEquals("7.5", stored.getCvss31Score());
            assertEquals("CVSS:3.1/AV:N", stored.getCvss31String());
            assertEquals("7.0", stored.getCvss40Score());
            assertEquals("CVSS:4.0/AV:N", stored.getCvss40String());

            CustomField cwe = stored.getCustomFields() == null ? null
                    : stored.getCustomFields().stream()
                            .filter(cf -> cf.getType() != null && "CWE".equals(cf.getType().getKey()))
                            .findFirst().orElse(null);
            assertNotNull("CSV CustomFields column must be imported", cwe);
            assertEquals("22", cwe.getValue());
        } finally {
            em.close();
        }
    }

    @Test
    public void uploadCSVRejectsRowWithoutNameOrCategory() {
        String csv = "Id,Name,CategoryId,CategoryName,Description,Recommendation\n"
                + "\"\",\"\",\"\",\"\",\"d\",\"r\"\n";
        Response response = new vulnerabilities().uploadDefaultCSVVulns(apiKey, csv);
        assertEquals(400, response.getStatus());
    }

    // --- GET /api/vulnerabilities/default/{id} (numeric path) ---

    @Test
    public void getDefaultVulnByIdReturnsRecord() {
        String name = "IT-Get-" + UUID.randomUUID().toString().substring(0, 8);
        Long vulnId = persistDefaultVuln(name);

        Response response = new vulnerabilities().getDefaultVulnByIdPath(apiKey, vulnId);
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity() instanceof DefaultVulnerabilityDTO);

        DefaultVulnerabilityDTO returned = (DefaultVulnerabilityDTO) response.getEntity();
        assertEquals(vulnId, returned.getId());
        assertEquals(name, returned.getName());
        assertEquals("integration test description", returned.getDescription());
    }

    @Test
    public void getDefaultVulnByIdReturns404ForUnknownId() {
        Response response = new vulnerabilities().getDefaultVulnByIdPath(apiKey, 777_777_777L);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void getDefaultVulnByIdReturns401WithoutValidApiKey() {
        Long vulnId = persistDefaultVuln("IT-GetAuth-" + UUID.randomUUID().toString().substring(0, 8));
        Response response = new vulnerabilities().getDefaultVulnByIdPath("not-a-key", vulnId);
        assertEquals(401, response.getStatus());
    }

    @Test
    public void searchDefaultByNameStillWorksForNonNumericPath() {
        String name = "IT-Search-" + UUID.randomUUID().toString().substring(0, 8);
        persistDefaultVuln(name);

        Response response = new vulnerabilities().searchdefault(apiKey, name);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        // Body is a JSON string with at least the matching name in it.
        String body = response.getEntity().toString();
        assertTrue("name search should include the persisted vuln: " + body,
                body.contains(name));
    }

    // --- Search regression: regex metacharacters and special separators ---

    @Test
    public void searchMatchesNameContainingParens() {
        // Regression: "(Timeroasting)" used to be parsed as a regex group, matching no parens
        // and excluding this row from results.
        String name = "IT-SNTP-" + UUID.randomUUID().toString().substring(0, 8)
                + " Information Disclosure (Timeroasting)";
        persistDefaultVuln(name);

        // Search by a substring that contains the parens.
        String term = "(Timeroasting)";
        Response response = new vulnerabilities().searchdefault(apiKey, term);
        assertEquals(200, response.getStatus());
        String body = response.getEntity().toString();
        assertTrue("search for literal parens should match the persisted name: " + body,
                body.contains(name));
    }

    @Test
    public void searchByQueryParamMatchesNameContainingForwardSlash() {
        // Regression: "LLMNR/NBT-NS" can't ride in a path param without being split, so
        // GET /default/search?name=... is the supported endpoint. Verify it matches.
        String name = "IT-LLMNR-" + UUID.randomUUID().toString().substring(0, 8)
                + " LLMNR/NBT-NS Spoofing - Machine Account Hash Capture";
        persistDefaultVuln(name);

        Response response = new vulnerabilities().searchDefaultByQuery(apiKey, "LLMNR/NBT-NS");
        assertEquals(200, response.getStatus());
        String body = response.getEntity().toString();
        assertTrue("query-param search must match name containing '/': " + body,
                body.contains(name));
    }

    @Test
    public void searchByQueryParamMatchesParensTooForConsistency() {
        String name = "IT-Paren-" + UUID.randomUUID().toString().substring(0, 8)
                + " (Timeroasting)";
        persistDefaultVuln(name);

        Response response = new vulnerabilities().searchDefaultByQuery(apiKey, "(Timeroasting)");
        assertEquals(200, response.getStatus());
        String body = response.getEntity().toString();
        assertTrue("query-param search should match parenthesized substring: " + body,
                body.contains(name));
    }

    @Test
    public void searchByQueryParamWithEmptyNameReturnsAll() {
        // Empty regex matches every name; mirrors the old path-param behavior.
        Response response = new vulnerabilities().searchDefaultByQuery(apiKey, "");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void searchRejectsAttemptedJsonInjection() {
        // A malicious term must not break out of the JSON string context. The query should
        // run safely and simply not match the literal payload.
        String injection = "\" }, \"$where\": \"true";
        Response response = new vulnerabilities().searchDefaultByQuery(apiKey, injection);
        assertEquals("server should not 5xx on injection attempt", 200, response.getStatus());
    }
}
