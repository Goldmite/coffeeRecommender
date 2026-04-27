import { redirect, type Handle } from '@sveltejs/kit';
import { paraglideMiddleware } from '$lib/paraglide/server';
import { getTextDirection } from '$lib/paraglide/runtime';
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

const authHandle: Handle = async ({ event, resolve }) => {
	const token = event.cookies.get('jwt');

	if (token) {
		try {
			const base64Payload = token.split('.')[1];

			const payload = JSON.parse(atob(base64Payload));
			const hasOnboardingCookie = event.cookies.get('onboarded') === 'true';

			event.locals.userId = payload.id;
			event.locals.userEmail = payload.sub;
			event.locals.isNew = hasOnboardingCookie ? false : payload.new;
		} catch (error) {
			event.locals.userId = undefined;
		}
	}

	const pathSegments = event.url.pathname.split('/').filter(Boolean);

	const isLangPrefix = pathSegments[0]?.length === 2;
	const rootPath = isLangPrefix ? pathSegments[1] : pathSegments[0];

	const isLoggedIn = !!event.locals.userId;

	const isAuthPage = rootPath === 'login' || rootPath === 'signup';
	const isProtectedRoute = rootPath === 'home' || rootPath === 'profile';

	if (!isLoggedIn && isProtectedRoute) {
		const lang = isLangPrefix ? `/${pathSegments[0]}` : '';
		throw redirect(303, `${lang}/login`);
	}

	if (isLoggedIn && isAuthPage) {
		const lang = isLangPrefix ? `/${pathSegments[0]}` : '';
		throw redirect(303, `${lang}/home`);
	}

	return resolve(event);
};

export const handle: Handle = sequence(paraglideHandle, authHandle);
