package org.datacite.mds.domain;

import javax.persistence.*;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;
import javax.validation.constraints.Min;
import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;
import org.datacite.mds.domain.Dataset;
import org.datacite.mds.validation.constraints.ValidXML;
import javax.validation.constraints.NotNull;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findMetadatasByDataset" })
public class Metadata {

    @Lob
    @Column(name = "xml", columnDefinition = "BLOB NOT NULL")
    @ValidXML
    private byte[] xml;

    @Min(0L)
    private Integer metadataVersion;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date lastUpdated;

    @NotNull
    @ManyToOne(targetEntity = Dataset.class)
    @JoinColumn
    private Dataset dataset;
}
