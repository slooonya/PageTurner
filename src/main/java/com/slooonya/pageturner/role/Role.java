package com.slooonya.pageturner.role;

import java.util.Set;

import com.slooonya.pageturner.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="roles")
@Getter 
@Setter
public class Role {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @ManyToMany(mappedBy="roles")
  private Set<User> users;

  public Role() {}

  public Role(String name) {
      this.name = name;
  }

  public Role (Long id, String name, Set<User> users) {
      this.id = id;
      this.name = name;
      this.users = users;
  }
}
