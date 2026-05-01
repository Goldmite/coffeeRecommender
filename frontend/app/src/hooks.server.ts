import { redirect, type Handle } from '@sveltejs/kit';
import { paraglideMiddleware } from '$lib/paraglide/server';
import { deLocalizeHref, getTextDirection, localizeHref } from '$lib/paraglide/runtime';
import { sequence } from '@sveltejs/kit/hooks';

const paraglideHandle: Handle = ({ event, resolve }) =>
	paraglideMiddleware(event.request, ({ request: localizedRequest, locale }) => {
		event.request = localizedRequest;
		return resolve(event, {
			transformPageChunk: ({ html }) => {
				return html.replace('%lang%', locale).replace('%dir%', getTextDirection(locale));
			},
		});
	});

const errorResponseHandle: Handle = async ({ event, resolve }) => {
	const response = await resolve(event);

	if (response.status === 401) {
		event.cookies.delete('jwt', { path: '/' });
		throw redirect(303, localizeHref('/login'));
	}
	if (response.status === 404) {
		throw redirect(303, localizeHref('/login'));
	}
	return response;
};

const AUTH_ROUTES = new Set(['/login', '/signup']);
const PROTECTED_ROUTES = new Set(['/home', '/profile', '/purchases']);

const authHandle: Handle = async ({ event, resolve }) => {
	const token = event.cookies.get('jwt');

	if (token) {
		try {
			const base64Payload = token.split('.')[1];

			const payload = JSON.parse(atob(base64Payload));
			const hasOnboardingCookie = event.cookies.get('onboarded') === 'true';

			event.locals.userId = payload.id;
			event.locals.userEmail = payload.sub;
			event.locals.username = payload.username;
			event.locals.isNew = hasOnboardingCookie ? false : payload.new;
		} catch (error) {
			event.locals.userId = undefined;
		}
	}
	const rootPath = deLocalizeHref(event.url.pathname);

	const isLoggedIn = !!event.locals.userId;

	const isAuthPage = AUTH_ROUTES.has(rootPath);
	const isProtectedRoute = PROTECTED_ROUTES.has(rootPath);

	if (!isLoggedIn && isProtectedRoute) {
		throw redirect(303, localizeHref('/login'));
	}

	if (isLoggedIn && isAuthPage) {
		throw redirect(303, localizeHref('/home'));
	}

	return resolve(event);
};

export const handle: Handle = sequence(paraglideHandle, errorResponseHandle, authHandle);
