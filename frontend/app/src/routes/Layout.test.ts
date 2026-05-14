import { render, screen } from '@testing-library/svelte';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import type { Snippet } from 'svelte';

import Layout from './+layout.svelte';

let locale = 'en';

const setLocale = vi.fn();

vi.mock('$lib/paraglide/runtime', () => ({
	getLocale: () => locale,
	setLocale: (l: string) => {
		locale = l;
		setLocale(l);
	},
	localizeHref: (href: string) => href,
}));

vi.mock('$lib/paraglide/messages.js', () => ({
	m: {
		coffee_recommender_system: () =>
			locale === 'lt' ? 'Kavos rekomendavimo sistema' : 'Coffee Recommender System',
	},
}));

vi.mock('$lib/components/core/SubmitButton.svelte', () => ({
	default: {},
}));

const children: Snippet = (() => {}) as unknown as Snippet;

describe('Layout language rendering (unit)', () => {
	beforeEach(() => {
		setLocale.mockClear();
	});

	it('renders EN title', () => {
		locale = 'en';

		render(Layout, {
			props: {
				data: { user: null },
				children,
			},
		});

		expect(screen.getByText('Coffee Recommender System')).toBeInTheDocument();
	});

	it('renders LT title', () => {
		locale = 'lt';

		render(Layout, {
			props: {
				data: { user: null },
				children,
			},
		});

		expect(screen.getByText('Kavos rekomendavimo sistema')).toBeInTheDocument();
	});
});
