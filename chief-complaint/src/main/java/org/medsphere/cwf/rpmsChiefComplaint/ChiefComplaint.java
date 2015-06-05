package org.medsphere.cwf.rpmsChiefComplaint;

import static org.carewebframework.common.StrUtil.U;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;

import org.carewebframework.cal.api.encounter.EncounterRelated;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.common.FhirUtil;
import org.carewebframework.rpms.api.domain.PCCUtil;

public class ChiefComplaint extends EncounterRelated {
    
    static {
        JSONUtil.registerAlias("ChiefComplaint", ChiefComplaint.class);
    }
    
    private Practitioner author;
    
    private String narrative;
    
    public ChiefComplaint() {
        super();
    }
    
    /**
     * Constructor to create a Chief Complaint from serialized form.
     * 
     * @param value V NARRATIVE IEN [1] ^ Author (IEN~Name) [2] ^ Line Count [3] Text Lines...
     *            ...repeatable 56^1~USER,DEMO^2 Patient has been anxious. 1.) SYM Minor Anxiety for
     *            1 Day. 57^1~USER,DEMO^1 1.) SYM Running Nose for 3 Days. 2.) Patient requests Cast
     *            Removal, Consult, Exam.
     * @param narrative Text narrative
     */
    public ChiefComplaint(String value, String narrative) {
        String[] pcs = StrUtil.split(value, U, 2);
        setId(pcs[0]);
        setAuthor(PCCUtil.parsePractitioner(pcs[1]));
        setNarrative(narrative);
    }
    
    private void setNarrative(String narrative) {
        this.narrative = narrative;
    }
    
    public String getNarrativeFormatted() {
        return narrative;
    }
    
    public Practitioner getAuthor() {
        return author;
    }
    
    public void setAuthor(Practitioner author) {
        this.author = author;
    }
    
    public String getAuthorName() {
        Practitioner author = getAuthor();
        return author == null ? "" : FhirUtil.formatName(author.getName());
    }
    
}
