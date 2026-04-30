import { error } from '@sveltejs/kit';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

interface InteractionParams {
	fetch: typeof fetch;
	token: string | undefined;
	userId: number | undefined;
	formData: FormData;
}

// userId - required
// coffeeId - required
// purchased - optional (boolean)
// rating - optional [1-5] (number)
export async function recordInteraction({ fetch, token, userId, formData }: InteractionParams) {
	if (!token || !userId) {
		throw error(401, 'Not authenticated');
	}

	formData.append('userId', userId.toString());

	const response = await fetch(`${PUBLIC_API_BASE_URL}/users/interactions`, {
		method: 'POST',
		headers: {
			Authorization: `Bearer ${token}`,
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(Object.fromEntries(formData)),
	});

	if (!response.ok) {
		throw error(response.status);
	}
}
