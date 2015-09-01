package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.cal.api.DomainObject;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;

public class Vaccine extends DomainObject{
	
	static {
		JSONUtil.registerAlias("VACCINE", Vaccine.class);
	}
	
	private String IEN;
	
	private String name;
	
	private String desc;
	
	private Boolean inactive;
	
	private String itemData;
	
	public Vaccine() {
		super();
	}
	
	/**
	 * Temporary constructor to create a Vaccine item from serialized form (will move to json).
	 * 
	 * @param value
	 *        Vaccine IEN [1] ^ Vaccine Name [2] ^ Vaccine Name [3] ^ Vaccine Description [4] ^ Inactive [5]
	 * 
	 */
	
	public Vaccine(String value) {
		String[] pcs = StrUtil.split(value, StrUtil.U, 5);
		setId(pcs[0]);
		setName(pcs[1]);
		setDesc(pcs[3]);
		setisInactive(pcs[4]);
		setItemData(value);
	}

	public void setisInactive(String inactive) {
		this.inactive = StrUtil.toBoolean(inactive);	
	}
	
	public Boolean isInactive() {
		return inactive;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setItemData(String data) {
		this.itemData = data;
	}
	
	public String getItemData() {
		return itemData;
	}

}
