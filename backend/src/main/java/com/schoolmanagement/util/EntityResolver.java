package com.schoolmanagement.util;

import com.schoolmanagement.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Shared helper for the "request body carries an {id-only} nested reference to
 * a related entity" pattern used by every service whose create/update method
 * takes a raw entity (e.g. {@code GradeRecord.student}, {@code
 * TeachingAssignment.schoolClass}): turn that id into the real, managed entity,
 * or fail loudly if it's missing/unknown.
 */
public final class EntityResolver {

    private EntityResolver() {
    }

    /**
     * @param id the referenced entity's id, or null if the caller sent no reference at all
     * @return the managed entity for that id
     * @throws ResourceNotFoundException (mapped to 404) if id is null or doesn't exist
     */
    public static <T, ID> T resolveOrThrow(JpaRepository<T, ID> repository, ID id, String entityLabel) {
        if (id == null) {
            throw new ResourceNotFoundException(entityLabel + " id is required");
        }
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityLabel + " not found with id: " + id));
    }
}
