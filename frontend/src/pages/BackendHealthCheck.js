import React, { useState, useEffect } from 'react';
import api from '../services/api';

function BackendHealthCheck() {
    const [status, setStatus] = useState('checking');
    const [details, setDetails] = useState({});

    useEffect(() => {
        checkBackend();
    }, []);

    const checkBackend = async () => {
        const results = {
            timestamp: new Date().toISOString(),
            checks: []
        };

        // Check 1: Grade Levels (existing endpoint)
        try {
            const res = await api.get('/grade-levels');
            results.checks.push({
                endpoint: '/grade-levels',
                status: 'OK',
                statusCode: res.status,
                dataCount: res.data?.length || 0
            });
        } catch (err) {
            results.checks.push({
                endpoint: '/grade-levels',
                status: 'FAILED',
                statusCode: err.response?.status || 'Network Error',
                error: err.response?.data?.message || err.message
            });
        }

        // Check 2: Classes (new endpoint)
        try {
            const res = await api.get('/classes');
            results.checks.push({
                endpoint: '/classes',
                status: 'OK',
                statusCode: res.status,
                dataCount: res.data?.length || 0
            });
        } catch (err) {
            results.checks.push({
                endpoint: '/classes',
                status: 'FAILED',
                statusCode: err.response?.status || 'Network Error',
                error: err.response?.data?.message || err.message
            });
        }

        // Check 3: Subjects (new endpoint)
        try {
            const res = await api.get('/subjects');
            results.checks.push({
                endpoint: '/subjects',
                status: 'OK',
                statusCode: res.status,
                dataCount: res.data?.length || 0
            });
        } catch (err) {
            results.checks.push({
                endpoint: '/subjects',
                status: 'FAILED',
                statusCode: err.response?.status || 'Network Error',
                error: err.response?.data?.message || err.message
            });
        }

        // Check 4: Assignments (new endpoint)
        try {
            const res = await api.get('/assignments');
            results.checks.push({
                endpoint: '/assignments',
                status: 'OK',
                statusCode: res.status,
                dataCount: res.data?.length || 0
            });
        } catch (err) {
            results.checks.push({
                endpoint: '/assignments',
                status: 'FAILED',
                statusCode: err.response?.status || 'Network Error',
                error: err.response?.data?.message || err.message
            });
        }

        // Check localStorage
        results.auth = {
            hasToken: !!localStorage.getItem('accessToken'),
            tokenLength: localStorage.getItem('accessToken')?.length || 0,
            user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null
        };

        setDetails(results);

        const allOk = results.checks.every(c => c.status === 'OK');
        setStatus(allOk ? 'healthy' : 'unhealthy');
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-4xl mx-auto">
                <div className="bg-white rounded-lg shadow-lg p-6">
                    <h1 className="text-2xl font-bold mb-4">Backend Health Check</h1>

                    <div className={`p-4 rounded mb-6 ${status === 'checking' ? 'bg-blue-100' :
                            status === 'healthy' ? 'bg-green-100' : 'bg-red-100'
                        }`}>
                        <p className="font-semibold">
                            Status: {status === 'checking' ? 'Checking...' : status.toUpperCase()}
                        </p>
                        <p className="text-sm text-gray-600">{details.timestamp}</p>
                    </div>

                    {/* Auth Status */}
                    <div className="mb-6">
                        <h2 className="text-xl font-semibold mb-2">Authentication</h2>
                        <div className="bg-gray-50 p-4 rounded">
                            <p>Has Token: {details.auth?.hasToken ? '✅ Yes' : '❌ No'}</p>
                            <p>Token Length: {details.auth?.tokenLength || 0} chars</p>
                            <p>User: {details.auth?.user?.username || 'Not logged in'}</p>
                            <p>Role: {details.auth?.user?.role || 'N/A'}</p>
                        </div>
                    </div>

                    {/* Endpoint Checks */}
                    <div>
                        <h2 className="text-xl font-semibold mb-2">API Endpoints</h2>
                        <div className="space-y-3">
                            {details.checks?.map((check, idx) => (
                                <div key={idx} className={`p-4 rounded border-l-4 ${check.status === 'OK' ? 'bg-green-50 border-green-500' : 'bg-red-50 border-red-500'
                                    }`}>
                                    <div className="flex justify-between items-start">
                                        <div>
                                            <p className="font-semibold">{check.endpoint}</p>
                                            <p className="text-sm text-gray-600">
                                                Status: {check.statusCode}
                                            </p>
                                            {check.status === 'OK' && (
                                                <p className="text-sm text-green-600">
                                                    ✅ {check.dataCount} records found
                                                </p>
                                            )}
                                            {check.status === 'FAILED' && (
                                                <p className="text-sm text-red-600">
                                                    ❌ {check.error}
                                                </p>
                                            )}
                                        </div>
                                        <span className={`px-3 py-1 rounded text-sm font-semibold ${check.status === 'OK' ? 'bg-green-200 text-green-800' : 'bg-red-200 text-red-800'
                                            }`}>
                                            {check.status}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="mt-6">
                        <button
                            onClick={checkBackend}
                            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                        >
                            Recheck
                        </button>
                    </div>

                    {/* Troubleshooting Tips */}
                    <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded">
                        <h3 className="font-semibold mb-2">Troubleshooting Tips:</h3>
                        <ul className="text-sm space-y-1 list-disc list-inside">
                            <li><strong>Network Error:</strong> Backend not running → Start with <code>mvn spring-boot:run</code></li>
                            <li><strong>404:</strong> Endpoint not found → Restart backend after creating controllers</li>
                            <li><strong>401:</strong> Not authenticated → Login again</li>
                            <li><strong>500:</strong> Server error → Check backend console logs</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default BackendHealthCheck;
