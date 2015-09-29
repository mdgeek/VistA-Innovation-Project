package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.cal.api.encounter.EncounterUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.common.FhirUtil;
import org.carewebframework.rpms.api.domain.Contraindication;
import org.carewebframework.rpms.api.domain.Forecast;
import org.carewebframework.rpms.api.domain.Immunization;
import org.carewebframework.rpms.api.domain.Refusal;
import org.carewebframework.rpms.ui.common.BgoConstants;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.FMDate;

import org.medsphere.cwf.rpmsImmunizations.MainController.EventType;

import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;

/*For an immunization:
   I ^ Imm Name [2] ^ Visit Date [3] ^ V File IEN [4] ^ Other Location [5] ^
   Group [6] ^ Imm IEN [7] ^ Lot [8] ^ Reaction [9] ^ VIS Date [10] ^
   Age [11] ^ Visit Date [12] ^ Provider IEN~Name [13] ^ Inj Site [14] ^
   Volume [15] ^ Visit IEN [16] ^ Visit Category [17] ^ Full Name [18] ^
   Location IEN~Name [19] ^ Visit Locked [20] ^ Event Date/Time [21] ^
   Dose Override [22] ^ VPED IEN [23] ^ VFC Eligibility [ 24] ^ Admin Notes [25] ^ Manufacturer [26]

Enter RETURN to continue or '^' to exit:
  
 For an immunization forecast:
   F ^ Imm Name [2] ^ Status [3]
  
 For a contraindication:
   C ^ Contra IEN [2] ^ Imm Name [3] ^ Reason [4] ^ Date [5]
  
 For a refusal:
   R ^ Refusal IEN [2] ^ Type IEN [3] ^ Type Name [4] ^ Item IEN [5] ^
   Item Name [6] ^ Provider IEN [7] ^ Provider Name [8] ^ Date [9] ^
   Locked [10] ^ Reason [11] ^ Comment [12]

 */
public class ImmunItem {
    
    protected Immunization immunization;
    
    protected Refusal refusal;
    
    protected Contraindication contraInd;
    
    protected Forecast forecast;
    
    public ImmunItem(String value) {
        char type = value.charAt(0);
        
        switch (type) {
            case 'I':
                immunization = new Immunization(value);
                break;
            case 'R':
                refusal = new Refusal(value);
                break;
            case 'C':
                contraInd = new Contraindication(value);
                break;
            case 'F':
                forecast = new Forecast(value);
                break;
                
        }
    }
    
    public EventType getEventType() {
        return refusal != null ? EventType.REFUSAL
                : getEncounter() == null || "E".equals(EncounterUtil.getServiceCategory(getEncounter()))
                        ? EventType.HISTORICAL : EventType.CURRENT;
    }
    
    public Encounter getEncounter() {
        return immunization != null ? immunization.getEncounter() : null;
    }
    
    public String getVisitIEN() {
        Encounter encounter = getEncounter();
        return encounter != null ? encounter.getId().getIdPart() : "";
    }
    
    public String getFileIEN() {
        if (immunization != null) {
            return immunization.getId().getIdPart();
        } else if (refusal != null) {
            return refusal.getId().getIdPart();
        } else if (contraInd != null) {
            return contraInd.getId().getIdPart();
        } else if (forecast != null) {
            return forecast.getId().getIdPart();
        }
        return "";
    }
    
    public boolean isLocked() {
        if (refusal != null) {
            return refusal.isLocked();
        } else if (immunization != null) {
            return immunization.getEncounter() == null ? true : EncounterUtil.isLocked(immunization.getEncounter());//false;
        } else {
            return false;
        }
    }
    
    public FMDate getDate() {
        if (refusal != null) {
            return refusal.getDate();
        } else if (contraInd != null) {
            return contraInd.getDate();
        } else if (immunization != null) {
            return immunization.getEventDate();
        }
        return null;
    }
    
    public String getVaccineID() {
        String res = "";
        if (immunization != null) {
            res = immunization.getImmunization().getId().getIdPart();
        } else {
            if (refusal != null) {
                res = refusal.getItem().getId().getIdPart();
            }
        }
        return res;
    }
    
    public String getVaccineName() {
        if (immunization != null) {
            return immunization.getImmunization().getProxiedObject().getDisplay();
        } else {
            if (refusal != null) {
                return refusal.getItem().getProxiedObject().getDisplay();
            } else {
                if (contraInd != null) {
                    return contraInd.getImmunization();
                } else {
                    if (forecast != null) {
                        return forecast.getImmunization();
                    }
                }
            }
        }
        return "";
    }
    
    public String getComment() {
        return refusal != null ? refusal.getComment() : "";
    }
    
    public String getStatus() {
        if (forecast != null) {
            return forecast.getStatus();
        }
        return "";
    }
    
    public String getReason() {
        if (contraInd != null) {
            return contraInd.getReason();
        } else {
            if (refusal != null) {
                return refusal.getReason();
            }
        }
        return "";
    }
    
    public String getAge() {
        return immunization != null ? immunization.getAge() : "";
    }
    
    public String getLocationName() {
        return immunization != null ? immunization.getLocation().getName() : "";
    }
    
    public String getLocationID() {
        return immunization != null ? immunization.getLocation().getId().getIdPart() : "";
    }
    
    public String getReaction() {
        return immunization != null ? immunization.getReaction() : "";
    }
    
    public String getVolume() {
        return immunization != null ? immunization.getVolume() : "";
    }
    
    public String getInjSite() {
        return immunization != null ? immunization.getSite() : "";
    }
    
    public String getLot() {
        return immunization != null ? immunization.getLot() : "";
    }
    
    public String getManuf() {
        return immunization != null ? immunization.getManufacturer() : "";
    }
    
    public FMDate getVISDate() {
        return immunization != null ? immunization.getVISDate() : null;
    }
    
    public Practitioner getAdminBy() {
        return refusal != null ? refusal.getProvider() : immunization != null ? immunization.getProvider() : null;
    }
    
    public String getVFCElig() {
        return immunization != null ? immunization.getVFCElig() : "";
    }
    
    public String getAdminNotes() {
        return immunization != null ? immunization.getAdminNotes() : "";
    }
    
    public String getVacOverride() {
        return immunization != null ? immunization.getVacOverride() : "";
    }
    
    public Practitioner getProvider() {
        
        if (immunization != null) {
            return immunization.getProvider();
        } else {
            if (refusal != null) {
                return refusal.getProvider();
            }
        }
        return null;
    }
    
    public Object getAdminByName() {
        Practitioner prv = getProvider();
        return prv == null ? "" : FhirUtil.formatName(prv.getName());
    }
    
    public boolean wasCounseled() {
        return StrUtil.extractInt(immunization.getVPEDIEN()) > 0;
    }
    
    public Boolean isRefusal() {
        return isType('R');
    }
    
    public Boolean isImmunization() {
        return isType('I');
    }
    
    public boolean isType(char type) {
        switch (type) {
            case 'I':
                return immunization != null;
            case 'R':
                return refusal != null;
            case 'C':
                return contraInd != null;
            case 'F':
                return forecast != null;
        }
        return false;
    }
    
    public boolean delete() {
        Practitioner provider = getProvider();
        IUser user = UserContext.getActiveUser();
        if (isImmunization()) {
            if (provider != null && !user.equals(provider)) {
                String s = VistAUtil.getBrokerSession().callRPC("BGOVIMM PRIPRV", immunization.getId().getIdPart());
                String[] pcs = StrUtil.split(s, StrUtil.U, 2);
                
                if (!user.getLogicalId().equals(pcs[0])) {
                    PromptDialog.showError("To delete the vaccination, you must either be the person that entered it or be "
                            + "designated as the primary provider for the visit.\n" + BgoConstants.TC_PRI_PRV + pcs[1]
                            + "\nExaminer: " + provider.getName(),
                        "Cannot Delete Vaccination");
                    return false;
                }
            }
        }
        
        if (PromptDialog.confirm(
            BgoConstants.TX_CNFM_DEL + getVaccineName() + " " + (isType('R') ? "refusal?" : "vaccination?"),
            "Remove Patient Vaccination?")) {
            PCC.errorCheck(VistAUtil.getBrokerSession().callRPC(isType('R') ? "BGOREF DEL" : "BGOVIMM DEL",
                VistAUtil.concatParams(immunization != null ? immunization.getId().getIdPart()
                        : refusal != null ? refusal.getId().getIdPart() : null)));
            return true;
        }
        return false;
    }
    
    public boolean deleteContra() {
        if (isType('C')) {
            if (PromptDialog.confirm(BgoConstants.TX_CNFM_DEL + "contraindication of "
                    + contraInd.getDate().toStringDateOnly() + " with " + contraInd.getReason() + " for this Patient?",
                "Remove Contraindication?")) {
                PCC.errorCheck(VistAUtil.getBrokerSession().callRPC("BGOVIMM DELCONT",
                    VistAUtil.concatParams(contraInd.getId().getIdPart())));
                return true;
            }
        }
        return false;
    }
    
}
