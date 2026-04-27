<script lang="ts">
	let { children } = $props();
</script>

<div class="bean-border overflow-hidden border border-main-border">
	{@render children()}
</div>
