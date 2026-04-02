<script lang="ts">
	import { enhance } from '$app/forms';
	import { replaceState } from '$app/navigation';
	import { page } from '$app/state';
	let { form } = $props();

	let isSuccessSignup = $derived(page.url.searchParams.get('signup_success') === 'true');
	$effect(() => {
		if (page.url.searchParams.has('signup_success')) {
			setTimeout(() => {
				const url = new URL(page.url);
				url.searchParams.delete('signup_success');
				replaceState(url, {});
			}, 5000);
		}
	});
</script>

{#if isSuccessSignup}
	<div class="success-banner">Account created successfully! Please log in.</div>
{/if}

<h1>Login</h1>

<form method="POST" use:enhance>
	{#if form?.authError}
		<div class="alert error">{form.authError}</div>
	{/if}
	{#if form?.error}
		<div class="alert error">{form.error}</div>
	{/if}
	<div class="field">
		<input
			name="email"
			type="email"
			value={form?.email ?? ''}
			required
			placeholder="Email"
			class:input-error={form?.validationErrors?.email}
		/>

		{#if form?.validationErrors?.email}
			<span class="error-text">{form.validationErrors.email}</span>
		{/if}
	</div>
	<div class="field">
		<input
			name="password"
			type="password"
			required
			placeholder="Password"
			class:input-error={form?.validationErrors?.password}
		/>
		{#if form?.validationErrors?.password}
			<span class="error-text">{form.validationErrors.password}</span>
		{/if}
	</div>

	<button type="submit">Login</button>
</form>

<style>
	.input-error {
		border: 2px solid red;
	}
	.error-text {
		color: red;
		font-size: 0.8rem;
	}
	.alert {
		padding: 1rem;
		background: #fee;
		border-radius: 4px;
		margin-bottom: 1rem;
	}
	.success-banner {
		background-color: #d4edda;
		color: #155724;
		padding: 1rem;
		border-radius: 4px;
		margin-bottom: 1rem;
		border: 1px solid #c3e6cb;
	}
</style>
