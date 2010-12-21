package org.datacite.mds.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.datacite.mds.util.Utils;
import org.datacite.mds.validation.constraints.Email;
import org.datacite.mds.validation.constraints.ListOfDomains;
import org.datacite.mds.validation.constraints.MatchSymbolPrefix;
import org.datacite.mds.validation.constraints.Symbol;
import org.datacite.mds.validation.constraints.Unique;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
@RooToString
@RooEntity(finders = { "findDatacentresBySymbolEquals", "findDatacentresByNameLike" })
@MatchSymbolPrefix
@Unique(field = "symbol")
@Entity
@XmlRootElement
public class Datacentre implements AllocatorOrDatacentre {

    private static Logger log4j = Logger.getLogger(Datacentre.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    
    @Version
    @Column(name = "version")
    private Integer version;
    
    @XmlTransient
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @XmlTransient
    public Integer getVersion() {
        return this.version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }    

    @NotNull
    @Symbol(Symbol.Type.DATACENTRE)
    @Column(unique = true)
    private String symbol;

    private String password;

    @NotNull
    @Size(min = 3, max = 255)
    private String name;

    @NotNull
    @Size(min = 2, max = 80)
    private String contactName;

    @NotNull
    @Email
    private String contactEmail;

    @NotNull
    private Integer doiQuotaAllowed = -1;

    @NotNull
    @Min(0L)
    @Max(999999999L)
    private Integer doiQuotaUsed = 0;

    private Boolean isActive = true;

    private String roleName = "ROLE_DATACENTRE";

    @XmlTransient
    public String getRoleName() {
        return this.roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Size(min = 0, max = 255)
    @ListOfDomains
    private String domains;

    @Size(max = 4000)
    private String comments;

    @NotNull
    @ManyToOne(targetEntity = Allocator.class)
    @JoinColumn
    private Allocator allocator;

    @XmlTransient 
    public Allocator getAllocator() {
        return this.allocator;
    }
    
    public void setAllocator(Allocator allocator) {
        this.allocator = allocator;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @OrderBy("prefix")
    @NotNull
    private Set<org.datacite.mds.domain.Prefix> prefixes = new java.util.HashSet<org.datacite.mds.domain.Prefix>();

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)

    private Date updated;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date created;

    @Transactional
    public void incQuotaUsed() {
        String qlString = "update Datacentre a set a.doiQuotaUsed = a.doiQuotaUsed + 1 where a.symbol = :symbol";
        entityManager.createQuery(qlString).setParameter("symbol", getSymbol()).executeUpdate();
    }
    
    @Transactional
    public boolean isQuotaExceeded() {
        return getDoiQuotaAllowed() <= getDoiQuotaUsed() && getDoiQuotaAllowed() >= 0;
    }

    @SuppressWarnings("unchecked")
    public static List<Datacentre> findAllDatacentresByAllocator(Allocator allocator) {
        String qlString = "select o from Datacentre o where allocator = :allocator order by symbol";
        return entityManager().createQuery(qlString).setParameter("allocator", allocator).getResultList();
    }

    @SuppressWarnings("unchecked")
    public static List<Datacentre> findDatacentreEntriesByAllocator(Allocator allocator, int firstResult, int maxResults) {
        String qlString = "select o from Datacentre o where allocator = :allocator order by symbol";
        return entityManager().createQuery(qlString).setParameter("allocator", allocator).setFirstResult(firstResult)
                .setMaxResults(maxResults).getResultList();
    }
    
    public static long countDatacentresByAllocator(Allocator allocator) {
        TypedQuery<Long> q = entityManager().createQuery("SELECT COUNT(*) FROM Datacentre WHERE allocator = :allocator", Long.class);
        q.setParameter("allocator", allocator);
        return q.getSingleResult();
    }

    @Transactional
    public void persist() {
        Date date = new Date();
        setCreated(date);
        setUpdated(date);
        if (this.entityManager == null)
            this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

    @Transactional
    public Datacentre merge() {
        setUpdated(new Date());
        if (this.entityManager == null)
            this.entityManager = entityManager();
        Datacentre merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    /**
     * retrieve a datacentre by symbol
     * @param symbol of an datacentre
     * @return datacentre with the given symbol or null if no such datacentre exists
     */
    public static Datacentre findDatacentreBySymbol(String symbol) {
        if (symbol == null) {
            return null;
        }
        try {
            log4j.debug("search for '" + symbol + "'");
            Datacentre dc = findDatacentresBySymbolEquals(symbol).getSingleResult();
            log4j.debug("found '" + symbol + "'");
            return dc;
        } catch (Exception e) {
            log4j.debug("no datacentre found");
            return null;
        }
    }
    
    /**
     * calculate String to be used for magic auth key
     * 
     * @return (unhashed) base part of the magic auth string
     */
    public String getBaseAuthString() {
        StringBuilder str = new StringBuilder();
        str.append(getId());
        str.append(getSymbol());
        str.append(getPassword());
        return str.toString();
    }

    public void setDomains(String domains) {
        this.domains = Utils.normalizeCsv(domains, Arrays.asList(" ", "\n"), true);
    }
}
