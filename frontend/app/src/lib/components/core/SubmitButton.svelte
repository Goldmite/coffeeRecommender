<script lang="ts">
	let { children, disabled = false } = $props();
</script>

<button class="btn-wide mt-4 bg-primary tracking-wide" type="submit" {disabled}
	>{@render children()}</button
>

<style>
	button[type='submit']:hover {
		box-shadow: var(--inset-shadow-md);
	}
</style>
