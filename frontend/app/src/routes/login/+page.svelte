<script lang="ts">
	import { login } from '$lib/api/auth';

	let email = '';
	let password = '';

	async function handleSubmit() {
		try {
			const user = await login({ email, password });
			console.log(user);
		} catch (error) {
			console.log(error);
		}
	}
</script>

<h1>Login</h1>

<form on:submit|preventDefault={handleSubmit}>
	<input bind:value={email} placeholder="Email" />
	<input type="password" bind:value={password} placeholder="Password" />
	<button type="submit">Login</button>
</form>
