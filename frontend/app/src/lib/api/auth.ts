import type { LoginRequest, SignupRequest, UserResponse } from '$lib/types/auth';
import { PUBLIC_BASE_URL } from '$env/static/public';

export async function login(data: LoginRequest): Promise<UserResponse> {
	const res = await fetch(`${PUBLIC_BASE_URL}/login`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(data),
	});

	if (!res.ok) {
		throw new Error('Login failed');
	}

	return res.json();
}

export async function signup(data: SignupRequest): Promise<UserResponse> {
	const res = await fetch(`${PUBLIC_BASE_URL}/signup`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(data),
	});

	if (!res.ok) {
		throw new Error('Signup failed');
	}

	return res.json();
}
