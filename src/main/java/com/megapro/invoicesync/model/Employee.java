package com.megapro.invoicesync.model;

import java.util.List;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee")
public class Employee extends UserApp {

    // @NotNull
    @Column(name = "nomorHp", unique = true)
    private String nomorHp;

    @Column(name = "first_name")
    private String first_name;

    @Column(name = "last_name")
    private String last_name;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postCode;

    @Column(name = "street")
    private String street;

    @Column(name = "photo")
    private String base64Photo = "https://img.freepik.com/premium-vector/new-woman-avatar-icon-flat-illustration-woman-avatar-vector-icon-any-web-design_98396-3382.jpg";

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "notification_counter")
    private int notificationCounter;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "image")
    private byte[] image;

    @OneToMany(mappedBy = "employee")
    @OrderBy("assignTime DESC")
    private List<Approval> listApproval;

    @OneToMany(mappedBy = "employee")
    @OrderBy("notificationId DESC")
    private List<Notification> listNotifications;
}
