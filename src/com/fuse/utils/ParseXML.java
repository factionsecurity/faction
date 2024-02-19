package com.fuse.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.MapItem;
import com.fuse.dao.ReportMap;
import com.fuse.dao.ReportMap.DataProperties;
import com.fuse.dao.VulnMap;
import com.fuse.dao.Vulnerability;

public class ParseXML {

	private void dig(Vulnerability vuln, String attr, DataProperties dp, Node node, List<MapItem> map,
			DefaultVulnerability genericVuln, Map<String, Long> severities, List<VulnMap> vulnMap) {
		MapItem mi;
		if (dp == null || dp == DataProperties.None)
			mi = map.stream().filter(m -> m.getParam().equals(attr)).findFirst().orElse(null);
		else
			mi = map.stream().filter(m -> m.getParam().equals(attr) && m.getProp() == dp).findFirst().orElse(null);
		if (mi == null)
			return;
		ReportMap.DataProperties name = mi.getProp();
		if (mi.isRecursive()) {
			NodeList list = node.getParentNode().getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				String newattr = n.getNodeName();
				MapItem rmi = map.stream().filter(m -> m.getParam().equals(newattr)).findFirst().orElse(null);
				if (rmi != null)
					dig(vuln, newattr, mi.getProp(), n.getFirstChild(), map, genericVuln, severities, vulnMap);
			}
		} else {

			Node n = node;
			if (n != null) {
				String value = n.getNodeValue();

				if (mi.isBase64()) {
					value = new String(Base64.getDecoder().decode(value));
					value = "<pre class='code'>" + value.replace("\n", "<br/>") + "</pre>";
					// value = new String(Base64.getEncoder().encodeToString(value.getBytes()));
				} /*
					 * else { value = "<pre class='code'>" + value + "</pre>"; //value = new
					 * String(Base64.getEncoder().encodeToString(value.getBytes())); }
					 */

				value = FSUtils.sanitizeHTML(value);
				switch (name) {
				case VulnName:
					vuln.setName(value);
					final String vname = value;
					if (vulnMap.stream().anyMatch(vmap -> vmap.getOriginTitle().equals(vname))) {
						VulnMap vm = vulnMap.stream().filter(vmap -> vmap.getOriginTitle().equals(vname)).findFirst()
								.orElse(null);
						vuln.setDefaultVuln(vm.getTargetVuln());
					} else {
						vuln.setDefaultVuln(genericVuln);
					}
					break;
				case VulnDescription:
					if (vuln.getDescription() == null)
						vuln.setDescription(value);
					else {
						vuln.setDescription(vuln.getDescription() + "<br/>" + value);
					}
					break;

				case Recommendation:
					if (vuln.getRecommendation() == null)
						vuln.setRecommendation(value);
					else {
						vuln.setRecommendation(vuln.getRecommendation() + "<br/>" + value);
					}
					break;
				case Severity:
					vuln.setOverall(severities.get(value));
					vuln.setLikelyhood(severities.get(value));
					vuln.setImpact(severities.get(value));
					break;
				default:
					;

				}
			}

		}
	}

	public List<Vulnerability> parseXML(File data, ReportMap map, Long asmtId, List<CustomType> customTypes,
			boolean removeDups) {
		return this.parseXML(data, asmtId, map.getListname(), map.getMapping(), map.getMapRating(), map.getVulnMap(),
				map.getDefaultVuln(), map.getCustomFields(), customTypes, removeDups);
	}

	public List<Vulnerability> parseXML(File data, Long asmtId, String startNodes, List<MapItem> map,
			Map<String, Long> severities, List<VulnMap> vulnMap, DefaultVulnerability genericVuln,
			Map<String, String> custFieldMap, List<CustomType> customTypes, boolean removeDups) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc;
		List<Vulnerability> vulns = new ArrayList();
		try {
			builder = factory.newDocumentBuilder();
			// InputSource is = new InputSource(new StringReader(data));

			doc = builder.parse(data);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return vulns;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return vulns;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return vulns;
		}

		// startNodes = "issue";
		NodeList nl = doc.getElementsByTagName(startNodes);

		for (int i = 0; i < nl.getLength(); i++) {

			NodeList cns = nl.item(i).getChildNodes();
			Vulnerability v = new Vulnerability();
			v.setAssessmentId(asmtId);
			for (int j = 0; j < cns.getLength(); j++) {

				Node n = cns.item(j).getFirstChild();
				if (n != null) {
					String value = n.getNodeValue();

					String attr = cns.item(j).getNodeName();
					// System.out.println("+++++++++"+cns.item(j).getNodeName());
					MapItem mi = map.stream().filter(m -> m.getParam().equals(attr)).findFirst().orElse(null);
					if (mi == null)
						continue;

					ReportMap.DataProperties name = mi.getProp();
					if (name == null) {
						if (custFieldMap.containsKey(attr)) {
							if (v.getCustomFields() == null)
								v.setCustomFields(new ArrayList());
							for (CustomType ct : customTypes) {
								if (ct.getVariable().equals(custFieldMap.get(attr))) {
									CustomField cf = new CustomField();
									cf.setType(ct);
									cf.setValue(value);
									v.getCustomFields().add(cf);
									break;
								}
							}
						}

						continue;
					}

					dig(v, attr, null, n, map, genericVuln, severities, vulnMap);

				}
			}
			vulns.add(v);
		}

		if (removeDups) {
			List<Vulnerability> tmpVulns = new ArrayList();
			for (Vulnerability v : vulns) {
				Vulnerability tmpVuln = tmpVulns.stream().filter(vp -> vp.getName().equals(v.getName())).findFirst()
						.orElse(null);
				if (tmpVuln == null) {
					tmpVulns.add(v);
				} 
			}
			return tmpVulns;

		} else
			return vulns;
	}
}
