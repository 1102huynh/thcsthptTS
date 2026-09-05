package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithPermissions(@Param("username") String username);

    // Backs GET /v1/users/search (D6 - admin account management page). Both
    // filters are optional (null = don't filter on it) so ADMIN can browse
    // every account, narrow to one role, search by name/username/email, or
    // combine both - `q` is passed in already lower-cased and wrapped in
    // "%...%" by the caller (AuthenticationService.searchUsers), not here,
    // so this stays a plain LIKE with no string-building in JPQL.
    @Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role = :role) "
            + "AND (:q IS NULL OR LOWER(u.username) LIKE :q OR LOWER(u.email) LIKE :q "
            + "OR LOWER(u.firstName) LIKE :q OR LOWER(u.lastName) LIKE :q)")
    Page<User> search(@Param("role") Role role, @Param("q") String q, Pageable pageable);
}

