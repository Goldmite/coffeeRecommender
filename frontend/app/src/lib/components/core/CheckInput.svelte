<script lang="ts">
	let {
		formId = undefined,
		idLabel,
		name,
		value,
		isChecked = false,
		labelText,
		colorScheme = 'primary',
		onchange = undefined,
	} = $props();
</script>

<label
	for={idLabel}
	class="bean-border flex cursor-pointer border border-main-border bg-main p-2
        hover:bg-main-mid/20 {colorScheme == 'primary'
		? 'has-checked:border-primary has-checked:bg-primary/20'
		: 'has-checked:border-secondary has-checked:bg-secondary/20'}"
>
	<input
		form={formId}
		id={idLabel}
		{name}
		{value}
		type="checkbox"
		class="hidden"
		checked={isChecked}
		onchange={onchange !== undefined ? (e) => onchange(e.currentTarget.checked) : undefined}
	/>

	<span>{labelText}</span>
</label>
