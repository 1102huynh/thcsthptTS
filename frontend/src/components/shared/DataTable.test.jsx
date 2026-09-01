import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DataTable from './DataTable';

const columns = [
  { accessorKey: 'name', header: 'Tên' },
  { accessorKey: 'age', header: 'Tuổi' },
  { id: 'actions', header: '', cell: () => <button aria-label="Sửa">edit</button> },
];

const data = [
  { id: 1, name: 'Nguyễn Văn A', age: 30 },
  { id: 2, name: 'Trần Thị B', age: 25 },
  { id: 3, name: 'Lê Văn C', age: 40 },
];

describe('DataTable', () => {
  it('renders column headers and row data', () => {
    render(<DataTable columns={columns} data={data} />);
    expect(screen.getByText('Tên')).toBeInTheDocument();
    expect(screen.getByText('Tuổi')).toBeInTheDocument();
    expect(screen.getByText('Nguyễn Văn A')).toBeInTheDocument();
    expect(screen.getByText('Trần Thị B')).toBeInTheDocument();
    expect(screen.getAllByRole('row')).toHaveLength(1 + data.length); // header + 3 body rows
  });

  it('names the actions column for screen readers even though its visible header is empty', () => {
    // Regression test for the Tuần 5 Ngày 5 axe fix: id: 'actions' columns
    // define header: '' (nothing to show visually - it's just icon
    // buttons), which used to leave the <th> with no accessible name at
    // all. DataTable substitutes sr-only text for that one column.
    render(<DataTable columns={columns} data={data} />);
    expect(screen.getByText('Thao tác')).toHaveClass('sr-only');
  });

  it('shows the empty message when there are no rows', () => {
    render(<DataTable columns={columns} data={[]} emptyMessage="Không tìm thấy." />);
    expect(screen.getByText('Không tìm thấy.')).toBeInTheDocument();
  });

  it('shows a skeleton instead of rows while loading, not the empty message', () => {
    render(<DataTable columns={columns} data={[]} isLoading emptyMessage="Không tìm thấy." />);
    expect(screen.queryByText('Không tìm thấy.')).not.toBeInTheDocument();
    // Skeleton rows render <td>s with no text content of their own.
    const rows = screen.getAllByRole('row');
    expect(rows.length).toBeGreaterThan(1);
  });

  it('sorts rows when a sortable header is clicked', async () => {
    const user = userEvent.setup();
    render(<DataTable columns={columns} data={data} />);
    const nameHeaderButton = screen.getByRole('button', { name: /Tên/ });

    await user.click(nameHeaderButton); // ascending
    let bodyRows = screen.getAllByRole('row').slice(1);
    expect(bodyRows[0]).toHaveTextContent('Lê Văn C');

    await user.click(nameHeaderButton); // descending
    bodyRows = screen.getAllByRole('row').slice(1);
    expect(bodyRows[0]).toHaveTextContent('Trần Thị B');
  });

  it('calls onSearchChange as the user types in the search box', async () => {
    const user = userEvent.setup();
    const onSearchChange = vi.fn();
    render(
      <DataTable
        columns={columns}
        data={data}
        searchValue=""
        onSearchChange={onSearchChange}
        searchPlaceholder="Tìm kiếm..."
      />
    );
    await user.type(screen.getByPlaceholderText('Tìm kiếm...'), 'A');
    expect(onSearchChange).toHaveBeenCalledWith('A');
  });

  it('does not render a search box when onSearchChange is not supplied', () => {
    render(<DataTable columns={columns} data={data} />);
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
  });

  describe('pagination', () => {
    it('renders the page range and disables Trang trước on the first page', () => {
      render(
        <DataTable
          columns={columns}
          data={data}
          pageIndex={0}
          pageCount={5}
          pageSize={10}
          totalCount={42}
          onPageChange={() => {}}
        />
      );
      expect(screen.getByText('Hiển thị 1–10 trong tổng số 42')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Trang trước' })).toBeDisabled();
      expect(screen.getByRole('button', { name: 'Trang sau' })).toBeEnabled();
    });

    it('disables Trang sau on the last page and calls onPageChange from Trang trước', async () => {
      const user = userEvent.setup();
      const onPageChange = vi.fn();
      render(
        <DataTable
          columns={columns}
          data={data}
          pageIndex={4}
          pageCount={5}
          pageSize={10}
          totalCount={42}
          onPageChange={onPageChange}
        />
      );
      expect(screen.getByRole('button', { name: 'Trang sau' })).toBeDisabled();

      await user.click(screen.getByRole('button', { name: 'Trang trước' }));
      expect(onPageChange).toHaveBeenCalledWith(3);
    });

    it('does not render pagination controls when pageCount is omitted', () => {
      render(<DataTable columns={columns} data={data} />);
      expect(screen.queryByRole('button', { name: 'Trang trước' })).not.toBeInTheDocument();
    });
  });
});
