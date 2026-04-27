<script lang="ts">
	let { children } = $props();
</script>

<div class="flex flex-1 justify-center">
	{@render children()}
</div>
