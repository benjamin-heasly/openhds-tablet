package org.openhds.mobile.model;

import java.io.Serializable;

public class Membership implements Serializable {

	private static final long serialVersionUID = 6446118055284774938L;

	private String individualExtId;
	private String socialGroupExtId;
	private String relationshipToHead;

	public String getIndividualExtId() {
		return individualExtId;
	}

	public void setIndividualExtId(String individualExtId) {
		this.individualExtId = individualExtId;
	}

	public String getSocialGroupExtId() {
		return socialGroupExtId;
	}

	public void setSocialGroupExtId(String socialGroupExtId) {
		this.socialGroupExtId = socialGroupExtId;
	}

	public String getRelationshipToHead() {
		return relationshipToHead;
	}

	public void setRelationshipToHead(String relationshipToHead) {
		this.relationshipToHead = relationshipToHead;
	}

}
