<script lang="ts">
	import { enhance } from '$app/forms';
	let { form } = $props();
</script>

<h1>Signup</h1>

<form method="POST" use:enhance>
	{#if form?.conflictError && form?.field}
		<div class="alert error">{form.conflictError}</div>
	{/if}
	{#if form?.error}
		<div class="alert error">{form.error}</div>
	{/if}
	<div class="field">
		<input
			name="name"
			type="text"
			value={form?.name ?? ''}
			required
			placeholder="Name"
			class:input-error={form?.validationErrors?.name}
		/>
		{#if form?.validationErrors?.name}
			<span class="error-text">{form.validationErrors.name}</span>
		{/if}
	</div>
	<div class="field">
		<input
			name="email"
			type="email"
			value={form?.email ?? ''}
			required
			placeholder="Email"
			class:input-error={form?.field === 'email' || form?.validationErrors?.email}
		/>
		{#if form?.field === 'email'}
			<span class="error-text">{form.conflictError}</span>
		{:else if form?.validationErrors?.email}
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

	<button type="submit">Signup</button>
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
</style>
