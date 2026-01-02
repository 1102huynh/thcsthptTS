import React, { useState, useEffect } from 'react';
import { FiBook, FiEdit, FiTrash2, FiPlus, FiSearch } from 'react-icons/fi';
import { libraryService } from '../services/dataService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function LibraryManagement() {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedBook, setSelectedBook] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [formData, setFormData] = useState({
        title: '',
        author: '',
        isbn: '',
        publisher: '',
        category: '',
        totalCopies: 1,
        availableCopies: 1,
        publicationYear: new Date().getFullYear()
    });

    useEffect(() => {
        fetchBooks();
    }, []);

    const fetchBooks = async () => {
        try {
            setLoading(true);
            const response = await libraryService.getBooks();
            setBooks(response.data || []);
        } catch (err) {
            setError('Failed to load books');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this book?')) {
            try {
                await libraryService.deleteBook(id);
                setBooks(books.filter(b => b.id !== id));
            } catch (err) {
                setError('Failed to delete book');
            }
        }
    };

    const handleAddNew = () => {
        setSelectedBook(null);
        setFormData({
            title: '',
            author: '',
            isbn: '',
            publisher: '',
            category: '',
            totalCopies: 1,
            availableCopies: 1,
            publicationYear: new Date().getFullYear()
        });
        setShowModal(true);
    };

    const handleEdit = (book) => {
        setSelectedBook(book);
        setFormData({
            title: book.title || '',
            author: book.author || '',
            isbn: book.isbn || '',
            publisher: book.publisher || '',
            category: book.category || '',
            totalCopies: book.totalCopies || 1,
            availableCopies: book.availableCopies || 1,
            publicationYear: book.publicationYear || new Date().getFullYear()
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (!formData.title || !formData.author || !formData.isbn) {
                setError('Please fill in all required fields');
                return;
            }

            if (selectedBook) {
                await libraryService.updateBook(selectedBook.id, formData);
            } else {
                await libraryService.createBook(formData);
            }
            setShowModal(false);
            setError('');
            fetchBooks();
        } catch (err) {
            setError('Failed to save book: ' + (err.message || 'Unknown error'));
        }
    };

    const filteredBooks = books.filter(book =>
        book.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        book.author?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        book.isbn?.includes(searchTerm)
    );

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-purple-50 to-violet-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiBook className="text-purple-600" />
                                Library Management
                            </h1>
                            <p className="text-gray-600">Manage your school library book inventory</p>
                        </div>
                        <Button onClick={handleAddNew} className="bg-gradient-to-r from-purple-600 to-violet-600 hover:from-purple-700 hover:to-violet-700">
                            <FiPlus className="w-4 h-4 mr-2" />
                            Add Book
                        </Button>
                    </div>
                </div>

                {/* Error Alert */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-800">{error}</p>
                    </div>
                )}

                {/* Search Bar */}
                <div className="mb-6">
                    <div className="relative">
                        <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                        <Input
                            placeholder="Search by title, author, or ISBN..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10"
                        />
                    </div>
                </div>

                {/* Books Table */}
                <Card className="border-0 shadow-xl">
                    <CardHeader>
                        <CardTitle>Books ({filteredBooks.length})</CardTitle>
                        <CardDescription>View and manage library inventory</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {loading ? (
                            <div className="flex justify-center items-center py-12">
                                <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead>
                                        <tr className="border-b border-gray-200">
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">ISBN</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Title</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Author</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Category</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Publisher</th>
                                            <th className="text-center py-3 px-4 text-sm font-semibold text-gray-700">Copies</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Status</th>
                                            <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {filteredBooks.length > 0 ? (
                                            filteredBooks.map((book) => (
                                                <tr key={book.id} className="hover:bg-gray-50 transition-colors">
                                                    <td className="py-4 px-4 text-sm text-gray-600">{book.isbn}</td>
                                                    <td className="py-4 px-4">
                                                        <div className="font-medium text-gray-900">{book.title}</div>
                                                    </td>
                                                    <td className="py-4 px-4 text-sm text-gray-600">{book.author}</td>
                                                    <td className="py-4 px-4">
                                                        <span className="px-2 py-1 bg-purple-100 text-purple-700 rounded text-xs font-medium">
                                                            {book.category}
                                                        </span>
                                                    </td>
                                                    <td className="py-4 px-4 text-sm text-gray-600">{book.publisher}</td>
                                                    <td className="py-4 px-4 text-center text-sm">
                                                        <span className="font-medium">{book.availableCopies}</span>
                                                        <span className="text-gray-400">/{book.totalCopies}</span>
                                                    </td>
                                                    <td className="py-4 px-4">
                                                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${book.availableCopies > 0
                                                                ? 'bg-green-100 text-green-700'
                                                                : 'bg-red-100 text-red-700'
                                                            }`}>
                                                            {book.availableCopies > 0 ? 'Available' : 'Out of Stock'}
                                                        </span>
                                                    </td>
                                                    <td className="py-4 px-4">
                                                        <div className="flex justify-end gap-2">
                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleEdit(book)}
                                                                className="hover:bg-purple-50 hover:text-purple-700"
                                                            >
                                                                <FiEdit className="w-4 h-4" />
                                                            </Button>
                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleDelete(book.id)}
                                                                className="hover:bg-red-50 hover:text-red-700"
                                                            >
                                                                <FiTrash2 className="w-4 h-4" />
                                                            </Button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="8" className="text-center py-12 text-gray-500">
                                                    {searchTerm ? 'No books found matching your search' : 'No books in library'}
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                            <CardHeader>
                                <CardTitle>{selectedBook ? 'Edit Book' : 'Add New Book'}</CardTitle>
                                <CardDescription>Fill in the book details</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="space-y-2">
                                    <Label htmlFor="title">Title *</Label>
                                    <Input
                                        id="title"
                                        value={formData.title}
                                        onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                                        placeholder="Enter book title"
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="author">Author *</Label>
                                        <Input
                                            id="author"
                                            value={formData.author}
                                            onChange={(e) => setFormData({ ...formData, author: e.target.value })}
                                            placeholder="Enter author name"
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="isbn">ISBN *</Label>
                                        <Input
                                            id="isbn"
                                            value={formData.isbn}
                                            onChange={(e) => setFormData({ ...formData, isbn: e.target.value })}
                                            placeholder="Enter ISBN"
                                        />
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="publisher">Publisher</Label>
                                        <Input
                                            id="publisher"
                                            value={formData.publisher}
                                            onChange={(e) => setFormData({ ...formData, publisher: e.target.value })}
                                            placeholder="Enter publisher"
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="category">Category</Label>
                                        <select
                                            id="category"
                                            value={formData.category}
                                            onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Category</option>
                                            <option value="Fiction">Fiction</option>
                                            <option value="Non-Fiction">Non-Fiction</option>
                                            <option value="Science">Science</option>
                                            <option value="Mathematics">Mathematics</option>
                                            <option value="History">History</option>
                                            <option value="Literature">Literature</option>
                                            <option value="Reference">Reference</option>
                                        </select>
                                    </div>
                                </div>

                                <div className="grid grid-cols-3 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="publicationYear">Year</Label>
                                        <Input
                                            id="publicationYear"
                                            type="number"
                                            value={formData.publicationYear}
                                            onChange={(e) => setFormData({ ...formData, publicationYear: parseInt(e.target.value) })}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="totalCopies">Total Copies</Label>
                                        <Input
                                            id="totalCopies"
                                            type="number"
                                            value={formData.totalCopies}
                                            onChange={(e) => setFormData({ ...formData, totalCopies: parseInt(e.target.value) })}
                                            min="1"
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="availableCopies">Available</Label>
                                        <Input
                                            id="availableCopies"
                                            type="number"
                                            value={formData.availableCopies}
                                            onChange={(e) => setFormData({ ...formData, availableCopies: parseInt(e.target.value) })}
                                            min="0"
                                        />
                                    </div>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>
                                        Cancel
                                    </Button>
                                    <Button onClick={handleSave} className="bg-gradient-to-r from-purple-600 to-violet-600">
                                        <FiPlus className="w-4 h-4 mr-2" />
                                        {selectedBook ? 'Update' : 'Save'}
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                )}
            </div>
        </div>
    );
}

export default LibraryManagement;
