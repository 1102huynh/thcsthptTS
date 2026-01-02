import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
    GraduationCap,
    Users,
    Award,
    TrendingUp,
    Calendar,
    ArrowRight,
    Phone,
    Mail,
    MapPin,
    Loader2,
    AlertCircle,
    Facebook,
    Twitter,
    Instagram,
    Linkedin
} from 'lucide-react';
import newsService from '../services/newsService';
import admissionService from '../services/admissionService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';

function PrincipalHomePage() {
    const [news, setNews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [admissions, setAdmissions] = useState([]);
    const [admissionsLoading, setAdmissionsLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('news');
    const [showBackToTop, setShowBackToTop] = useState(false);
    const [scrolled, setScrolled] = useState(false);

    const fetchNews = async (page = 0) => {
        try {
            setLoading(true);
            setError(null);
            const response = await newsService.getPublishedNews(page, 3);
            setNews(response.content || []);
        } catch (err) {
            console.error('Error fetching news:', err);
            setError('Unable to load news');
            // Fallback data
            setNews([
                {
                    id: 1,
                    title: 'Annual Sports Day 2025',
                    content: 'Join us for the exciting Annual Sports Day on December 15, 2025. Various sports competitions and cultural programs will be held.',
                    publishedDate: '2025-11-16',
                    category: 'Event',
                },
                {
                    id: 2,
                    title: 'New Computer Lab Inauguration',
                    content: 'Our state-of-the-art computer lab with 50 high-end computers is now operational.',
                    publishedDate: '2025-11-15',
                    category: 'Infrastructure',
                },
                {
                    id: 3,
                    title: 'Excellence Awards Ceremony',
                    content: 'Annual Excellence Awards ceremony will be held on November 30, 2025.',
                    publishedDate: '2025-11-14',
                    category: 'Achievement',
                },
            ]);
        } finally {
            setLoading(false);
        }
    };

    const fetchAdmissions = async () => {
        try {
            setAdmissionsLoading(true);
            const response = await admissionService.getOpenAdmissions(0, 10);
            setAdmissions(response.content || []);
        } catch (err) {
            console.error('Error fetching admissions:', err);
            setAdmissions([]);
        } finally {
            setAdmissionsLoading(false);
        }
    };

    useEffect(() => {
        fetchNews(0);
        fetchAdmissions();

        // Scroll event listener
        const handleScroll = () => {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            setShowBackToTop(scrollTop > 300);
            setScrolled(scrollTop > 50);
        };

        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const statistics = [
        { icon: Users, value: '5000+', label: 'Students Enrolled', gradient: 'from-blue-500 to-purple-600' },
        { icon: Award, value: '150+', label: 'Expert Faculty', gradient: 'from-pink-500 to-rose-600' },
        { icon: Calendar, value: '25+', label: 'Years Excellence', gradient: 'from-cyan-500 to-blue-600' },
        { icon: TrendingUp, value: '100%', label: 'Success Rate', gradient: 'from-orange-500 to-pink-600' },
    ];

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-purple-50">
            {/* Navbar */}
            <nav className={`bg-white/80 backdrop-blur-md border-b border-gray-200 sticky top-0 z-50 transition-shadow duration-300 ${scrolled ? 'shadow-lg' : 'shadow-sm'
                }`}>
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center space-x-3">
                            <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center">
                                <GraduationCap className="w-6 h-6 text-white" />
                            </div>
                            <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                                Tay Son School
                            </span>
                        </div>
                        <div className="flex items-center space-x-4">
                            <Link to="/login">
                                <Button className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700">
                                    <ArrowRight className="w-4 h-4 mr-2" />
                                    Login Portal
                                </Button>
                            </Link>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Hero Section */}
            <div className="relative overflow-hidden bg-gradient-to-br from-blue-600 via-purple-600 to-pink-600 text-white">
                {/* Animated background elements */}
                <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-20">
                    <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-white rounded-full mix-blend-overlay filter blur-3xl animate-blob"></div>
                    <div className="absolute top-1/3 right-1/4 w-96 h-96 bg-blue-300 rounded-full mix-blend-overlay filter blur-3xl animate-blob animation-delay-2000"></div>
                    <div className="absolute bottom-1/4 left-1/3 w-96 h-96 bg-purple-300 rounded-full mix-blend-overlay filter blur-3xl animate-blob animation-delay-4000"></div>
                </div>

                <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32">
                    <div className="text-center">
                        <div className="inline-block mb-4 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-full text-sm font-semibold">
                            🏆 Ranked #1 School in the Region
                        </div>
                        <h1 className="text-4xl md:text-6xl font-bold mb-6 leading-tight">
                            Tay Son Secondary<br />& High School
                        </h1>
                        <p className="text-xl md:text-2xl text-blue-100 mb-8 max-w-3xl mx-auto">
                            Empowering minds, shaping futures. Quality education for a brighter tomorrow.
                        </p>
                    </div>
                </div>

                {/* Wave separator */}
                <div className="absolute bottom-0 left-0 right-0">
                    <svg viewBox="0 0 1440 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M0 120L60 105C120 90 240 60 360 45C480 30 600 30 720 37.5C840 45 960 60 1080 67.5C1200 75 1320 75 1380 75L1440 75V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0Z" fill="#F8FAFC" />
                    </svg>
                </div>
            </div>

            {/* Statistics Section */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-16 relative z-10 mb-20">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {statistics.map((stat, index) => (
                        <Card key={index} className="border-0 shadow-xl hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 bg-white/80 backdrop-blur-sm">
                            <CardContent className="p-6">
                                <div className={`w-12 h-12 bg-gradient-to-br ${stat.gradient} rounded-lg flex items-center justify-center mb-4`}>
                                    <stat.icon className="w-6 h-6 text-white" />
                                </div>
                                <div className="text-3xl font-bold text-gray-900 mb-1">{stat.value}</div>
                                <div className="text-sm text-gray-600">{stat.label}</div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            </div>

            {/* Tabs Section */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-20">
                {/* Tab Navigation */}
                <div className="flex justify-center mb-8 space-x-4">
                    <button
                        onClick={() => setActiveTab('news')}
                        className={`px-6 py-3 rounded-lg font-semibold transition-all ${activeTab === 'news'
                            ? 'bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg'
                            : 'bg-white text-gray-600 hover:bg-gray-50'
                            }`}
                    >
                        📰 News & Updates
                    </button>
                    <button
                        onClick={() => setActiveTab('admissions')}
                        className={`px-6 py-3 rounded-lg font-semibold transition-all ${activeTab === 'admissions'
                            ? 'bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg'
                            : 'bg-white text-gray-600 hover:bg-gray-50'
                            }`}
                    >
                        🎓 Admissions
                    </button>
                    <button
                        onClick={() => setActiveTab('about')}
                        className={`px-6 py-3 rounded-lg font-semibold transition-all ${activeTab === 'about'
                            ? 'bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg'
                            : 'bg-white text-gray-600 hover:bg-gray-50'
                            }`}
                    >
                        ℹ️ About Us
                    </button>
                </div>

                {/* Tab Content */}
                {activeTab === 'news' && (
                    <div>
                        <div className="text-center mb-12">
                            <h2 className="text-3xl font-bold text-gray-900 mb-4">Latest News & Announcements</h2>
                            <p className="text-gray-600">Stay updated with our school activities and achievements</p>
                        </div>

                        {loading ? (
                            <div className="flex justify-center items-center py-20">
                                <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
                            </div>
                        ) : error ? (
                            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-start gap-3 mb-6">
                                <AlertCircle className="w-5 h-5 text-yellow-600 mt-0.5 flex-shrink-0" />
                                <p className="text-sm text-yellow-800">{error}</p>
                            </div>
                        ) : null}

                        <div className="space-y-6">
                            {news.map((item) => (
                                <Card key={item.id} className="hover:shadow-xl transition-all duration-300 border-0 bg-white/80 backdrop-blur-sm">
                                    <CardContent className="p-6">
                                        <div className="flex items-start gap-4">
                                            <div className="flex-shrink-0 w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-3xl">
                                                {item.image || '📰'}
                                            </div>
                                            <div className="flex-1">
                                                <div className="flex items-center gap-2 mb-2">
                                                    <span className="px-3 py-1 bg-blue-100 text-blue-600 rounded-full text-xs font-semibold">
                                                        {item.category}
                                                    </span>
                                                    <span className="text-sm text-gray-500 flex items-center gap-1">
                                                        <Calendar className="w-4 h-4" />
                                                        {new Date(item.publishedDate).toLocaleDateString('en-US', {
                                                            year: 'numeric',
                                                            month: 'long',
                                                            day: 'numeric'
                                                        })}
                                                    </span>
                                                </div>
                                                <h3 className="text-xl font-bold text-gray-900 mb-2">{item.title}</h3>
                                                <p className="text-gray-600 mb-4">
                                                    {item.content.length > 200 ? `${item.content.substring(0, 200)}...` : item.content}
                                                </p>
                                                <Link to={`/news/${item.id}`}>
                                                    <Button variant="outline" size="sm" className="group">
                                                        Read More
                                                        <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
                                                    </Button>
                                                </Link>
                                            </div>
                                        </div>
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    </div>
                )}

                {activeTab === 'admissions' && (
                    <div>
                        <div className="text-center mb-12">
                            <h2 className="text-3xl font-bold text-gray-900 mb-4">Admission Information</h2>
                            <p className="text-gray-600">Join our community of excellence</p>
                        </div>

                        {admissionsLoading ? (
                            <div className="flex justify-center items-center py-20">
                                <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
                            </div>
                        ) : admissions.length === 0 ? (
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-8 text-center">
                                <p className="text-blue-800">No admission information available at this time.</p>
                            </div>
                        ) : (
                            <div className="space-y-6">
                                {admissions.map((admission) => (
                                    <Card key={admission.id} className="border-0 shadow-xl bg-white/80 backdrop-blur-sm">
                                        <CardHeader>
                                            <div className="flex justify-between items-start">
                                                <div>
                                                    <CardTitle className="text-2xl mb-2">{admission.title}</CardTitle>
                                                    <div className="flex gap-2">
                                                        <span className="px-3 py-1 bg-green-100 text-green-600 rounded-full text-xs font-semibold">
                                                            {admission.status}
                                                        </span>
                                                        <span className="px-3 py-1 bg-blue-100 text-blue-600 rounded-full text-xs font-semibold">
                                                            {admission.grade}
                                                        </span>
                                                    </div>
                                                </div>
                                                <Calendar className="w-10 h-10 text-blue-600" />
                                            </div>
                                        </CardHeader>
                                        <CardContent>
                                            <p className="text-gray-600 mb-6">{admission.description}</p>

                                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                                                <div className="bg-gradient-to-br from-blue-50 to-purple-50 p-4 rounded-lg">
                                                    <div className="text-sm text-gray-600 mb-1">Total Seats</div>
                                                    <div className="text-2xl font-bold text-blue-600">{admission.seats}</div>
                                                </div>
                                                <div className="bg-gradient-to-br from-pink-50 to-orange-50 p-4 rounded-lg">
                                                    <div className="text-sm text-gray-600 mb-1">Deadline</div>
                                                    <div className="text-lg font-bold text-pink-600">
                                                        {new Date(admission.deadline).toLocaleDateString()}
                                                    </div>
                                                </div>
                                            </div>

                                            <Button className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700">
                                                <Mail className="w-4 h-4 mr-2" />
                                                Apply Now
                                            </Button>
                                        </CardContent>
                                    </Card>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                {activeTab === 'about' && (
                    <div>
                        <div className="text-center mb-12">
                            <h2 className="text-3xl font-bold text-gray-900 mb-4">About Our Institution</h2>
                            <p className="text-gray-600">Building tomorrow's leaders today</p>
                        </div>

                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            <Card className="lg:col-span-2 border-0 shadow-xl bg-white/80 backdrop-blur-sm">
                                <CardHeader>
                                    <CardTitle>🎯 Our Mission</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p className="text-gray-600">
                                        To provide quality education that develops critical thinking, creativity, and character
                                        in our students, preparing them to be responsible global citizens who can contribute
                                        meaningfully to society.
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-0 shadow-xl bg-gradient-to-br from-blue-600 to-purple-600 text-white">
                                <CardHeader>
                                    <CardTitle className="text-white">📞 Contact Info</CardTitle>
                                </CardHeader>
                                <CardContent className="space-y-4">
                                    <div className="flex items-start gap-3">
                                        <MapPin className="w-5 h-5 flex-shrink-0 mt-1" />
                                        <div>
                                            <div className="font-semibold">Address</div>
                                            <div className="text-blue-100 text-sm">Tay Son District, Vietnam</div>
                                        </div>
                                    </div>
                                    <div className="flex items-start gap-3">
                                        <Phone className="w-5 h-5 flex-shrink-0 mt-1" />
                                        <div>
                                            <div className="font-semibold">Phone</div>
                                            <div className="text-blue-100 text-sm">+84 (123) 456-7890</div>
                                        </div>
                                    </div>
                                    <div className="flex items-start gap-3">
                                        <Mail className="w-5 h-5 flex-shrink-0 mt-1" />
                                        <div>
                                            <div className="font-semibold">Email</div>
                                            <div className="text-blue-100 text-sm">info@taysonsecondary.edu</div>
                                        </div>
                                    </div>
                                </CardContent>
                            </Card>
                        </div>
                    </div>
                )}
            </div>

            {/* Footer */}
            <footer className="bg-gray-900 text-white">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                        <div className="md:col-span-2">
                            <div className="flex items-center space-x-3 mb-4">
                                <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center">
                                    <GraduationCap className="w-6 h-6 text-white" />
                                </div>
                                <span className="text-xl font-bold">Tay Son School</span>
                            </div>
                            <p className="text-gray-400 mb-4">
                                Empowering students with quality education and preparing them for a successful future.
                            </p>
                            <div className="flex gap-3">
                                <a href="#" className="w-10 h-10 bg-white/10 hover:bg-white/20 rounded-lg flex items-center justify-center transition-colors">
                                    <Facebook className="w-5 h-5" />
                                </a>
                                <a href="#" className="w-10 h-10 bg-white/10 hover:bg-white/20 rounded-lg flex items-center justify-center transition-colors">
                                    <Twitter className="w-5 h-5" />
                                </a>
                                <a href="#" className="w-10 h-10 bg-white/10 hover:bg-white/20 rounded-lg flex items-center justify-center transition-colors">
                                    <Instagram className="w-5 h-5" />
                                </a>
                                <a href="#" className="w-10 h-10 bg-white/10 hover:bg-white/20 rounded-lg flex items-center justify-center transition-colors">
                                    <Linkedin className="w-5 h-5" />
                                </a>
                            </div>
                        </div>

                        <div>
                            <h3 className="font-semibold mb-4">Quick Links</h3>
                            <ul className="space-y-2 text-gray-400">
                                <li><Link to="/login" className="hover:text-white transition-colors">Login Portal</Link></li>
                                <li><a href="#" className="hover:text-white transition-colors">Admissions</a></li>
                                <li><a href="#" className="hover:text-white transition-colors">News & Events</a></li>
                                <li><a href="#" className="hover:text-white transition-colors">About Us</a></li>
                            </ul>
                        </div>

                        <div>
                            <h3 className="font-semibold mb-4">Office Hours</h3>
                            <div className="text-gray-400 space-y-2">
                                <div>
                                    <div className="text-sm">Monday - Friday</div>
                                    <div className="font-semibold text-white">8:00 AM - 4:00 PM</div>
                                </div>
                                <div>
                                    <div className="text-sm">Saturday</div>
                                    <div className="font-semibold text-white">8:00 AM - 12:00 PM</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400 text-sm">
                        <p>© 2025 Tay Son Secondary and High School. All rights reserved.</p>
                    </div>
                </div>
            </footer>

            <style jsx>{`
        @keyframes blob {
          0% { transform: translate(0px, 0px) scale(1); }
          33% { transform: translate(30px, -50px) scale(1.1); }
          66% { transform: translate(-20px, 20px) scale(0.9); }
          100% { transform: translate(0px, 0px) scale(1); }
        }
        .animate-blob {
          animation: blob 7s infinite;
        }
        .animation-delay-2000 {
          animation-delay: 2s;
        }
        .animation-delay-4000 {
          animation-delay: 4s;
        }
      `}</style>

            {/* Back to Top Button */}
            {showBackToTop && (
                <button
                    onClick={scrollToTop}
                    className="fixed bottom-8 right-8 w-14 h-14 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-full shadow-2xl hover:shadow-3xl transition-all transform hover:scale-110 z-50 flex items-center justify-center group"
                    aria-label="Back to top"
                >
                    <ArrowRight className="w-6 h-6 -rotate-90 group-hover:-translate-y-1 transition-transform" />
                </button>
            )}
        </div>
    );
}

export default PrincipalHomePage;
