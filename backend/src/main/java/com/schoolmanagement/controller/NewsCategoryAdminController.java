package com.schoolmanagement.controller;

import com.schoolmanagement.dto.NewsCategoryDTO;
import com.schoolmanagement.dto.NewsCategoryRequest;
import com.schoolmanagement.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/news-categories")
@AllArgsConstructor
@Tag(name = "News (CMS)", description = "Chuyên mục tin — ADMIN/PRINCIPAL")
public class NewsCategoryAdminController {

    private NewsService newsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Danh sách chuyên mục")
    public ResponseEntity<List<NewsCategoryDTO>> list() {
        return new ResponseEntity<>(newsService.listCategories(), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Tạo chuyên mục (slug tự sinh)")
    public ResponseEntity<NewsCategoryDTO> create(@Valid @RequestBody NewsCategoryRequest request) {
        return new ResponseEntity<>(newsService.createCategory(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Sửa chuyên mục (slug giữ nguyên)")
    public ResponseEntity<NewsCategoryDTO> update(@PathVariable Long id, @Valid @RequestBody NewsCategoryRequest request) {
        return new ResponseEntity<>(newsService.updateCategory(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Xoá chuyên mục (bài tin thuộc chuyên mục sẽ về 'chưa phân loại')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        newsService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
